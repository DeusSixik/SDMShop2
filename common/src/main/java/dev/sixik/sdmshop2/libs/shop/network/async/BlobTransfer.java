package dev.sixik.sdmshop2.libs.shop.network.async;

import dev.architectury.networking.NetworkManager;
import dev.sixik.sdmshop2.SDMShop2;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BlobTransfer {

    public static final ResourceLocation CHANNEL = new ResourceLocation(SDMShop2.MODID, "blob_channel");

    /**
     * Optimal chunk size: 50 KB.
     * Too little = overhead on headers. Too much (1MB+) = network freezes.
     */
    private static final int CHUNK_SIZE = 50 * 1024;

    private static class BlobReceiver {
        final ByteBuf buffer = Unpooled.buffer();
        int receivedChunks = 0;
        long lastUpdateTime = System.currentTimeMillis();
    }

    private static final Map<Long, BlobReceiver> INCOMING_BUFFERS = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService CLEANUP_SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    static {
        CLEANUP_SCHEDULER.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            INCOMING_BUFFERS.entrySet().removeIf(entry -> {
                if (now - entry.getValue().lastUpdateTime > 30000) {
                    entry.getValue().buffer.release();
                    return true;
                }
                return false;
            });
        }, 10, 10, TimeUnit.SECONDS);
    }

    public static void initServer() {
        NetworkManager.registerReceiver(NetworkManager.Side.C2S, CHANNEL, BlobTransfer::onPacket);
    }

    @Environment(EnvType.CLIENT)
    public static void initClient() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CHANNEL, BlobTransfer::onPacket);
    }

    /**
     * Sends large amounts of data. Returns a Future that will complete when (theoretically) the transfer is finished.
     * But for “request-response,” how we receive the data is more important to us.
     */
    public static void sendToPlayer(ServerPlayer player, long responseId, FriendlyByteBuf hugeData) {
        sendInternal(hugeData, responseId, buf -> NetworkManager.sendToPlayer(player, CHANNEL, buf));
    }

    public static void sendToServer(long requestId, FriendlyByteBuf hugeData) {
        sendInternal(hugeData, requestId, buf -> NetworkManager.sendToServer(CHANNEL, buf));
    }

    private static void sendInternal(ByteBuf data, long id, Consumer<FriendlyByteBuf> sender) {
        int totalSize = data.readableBytes();
        int chunks = (int) Math.ceil((double) totalSize / CHUNK_SIZE);

        /*
             We use slice to avoid copying memory (Zero-Copy when reading)
         */
        for (int i = 0; i < chunks; i++) {
            int offset = i * CHUNK_SIZE;
            int length = Math.min(CHUNK_SIZE, totalSize - offset);

            FriendlyByteBuf packet = new FriendlyByteBuf(Unpooled.buffer());
            packet.writeLong(id);       // Request ID
            packet.writeInt(i);         // Current chunk index
            packet.writeInt(chunks);    // Total pieces

            /*
                We record a piece of data
             */
            packet.writeBytes(data, offset, length);

            sender.accept(packet);
        }

        data.release();
    }

    private static void onPacket(FriendlyByteBuf buf, NetworkManager.PacketContext context) {
        long id = buf.readLong();
        int chunkIndex = buf.readInt();
        int totalChunks = buf.readInt();

        /*
            Obtain or create a buffer for assembly
         */
        BlobReceiver receiver = INCOMING_BUFFERS.computeIfAbsent(id, k -> new BlobReceiver());
        receiver.lastUpdateTime = System.currentTimeMillis();

        /*
            IMPORTANT: Netty packets may arrive out of order (rarely, but it happens in UDP; in TCP, MC
            guarantees order, but it is better to write to the end, relying on sequential sending).
            Here, we simply write to the end, since TCP guarantees byte order.
         */
        receiver.buffer.writeBytes(buf);
        receiver.receivedChunks++;

        /*
            If this is the last piece
         */
        if (receiver.receivedChunks == totalChunks) {
            INCOMING_BUFFERS.remove(id);

            /*
                If this was a response to our AsyncBridge request -> we complete it
             */
            FriendlyByteBuf fullData = new FriendlyByteBuf(receiver.buffer);
            context.queue(() -> completeBridgeRequest(id, fullData));
        }
    }

    private static void completeBridgeRequest(long id, FriendlyByteBuf data) {
        AsyncBridge.completeExternal(id, data);
    }
}
