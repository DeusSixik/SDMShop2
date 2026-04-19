package dev.sixik.sdmshop2.libs.shop.network;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTable;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncBridge;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncServerTasks;
import dev.sixik.sdmshop2.utils.ShopUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.Function;

public class ShopNetworkManager {

    public static void sendShopData(ShopInstance shop, ServerPlayer... players) {
        sendShopData(buf -> {
            shop.serializeNetwork(buf);
            return buf;
        }, players);
    }

    private static void sendShopData(Function<FriendlyByteBuf, FriendlyByteBuf> writer, ServerPlayer... players) {
        for (int i = 0; i < players.length; i++) {
            AsyncBridge.askPlayer(players[i], AsyncServerTasks.SEND_SHOP_DATA, writer);
        }
    }

    public static void sendLimiterData(ServerPlayer... players) {
        final var opt = ShopUtils.getLimiterTable(false);
        if(opt.isEmpty()) return;

        final ShopLimiterTable limiterTable = opt.get();
        for (int i = 0; i < players.length; i++) {
            final ServerPlayer player = players[i];
            AsyncBridge.askPlayer(player, AsyncServerTasks.SEND_SHOP_LIMITER_DATA, buf -> {
                limiterTable.toNetwork(player.getGameProfile().getId(), buf);
                return buf;
            });
        }
    }
}
