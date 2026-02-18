package dev.sixik.sdmshop2.libs.sdmeconomy.network.packets;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.sixik.sdmshop2.libs.sdmeconomy.BankAccount;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyService;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyServiceClient;
import dev.sixik.sdmshop2.libs.sdmeconomy.network.SDMEconomyNetwork;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SendPlayerAccountS2C extends BaseS2CMessage {

    private final CompoundTag accountNbt;

    public SendPlayerAccountS2C(ServerPlayer player) {
        final BankAccount data = SDMEconomyService.getInstance().getAccount(player.getGameProfile().getId());
        this.accountNbt = data.serializeNbt();
    }

    public SendPlayerAccountS2C(FriendlyByteBuf buf) {
        this.accountNbt = buf.readAnySizeNbt();
    }

    @Override
    public MessageType getType() {
        return SDMEconomyNetwork.SEND_PLAYER_ACCOUNT;
    }

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeNbt(accountNbt);
    }

    @Override
    public void handle(NetworkManager.PacketContext packetContext) {
        try {
            SDMEconomyServiceClient.getInstanceClient().getBankAccount().deserializeNbt(accountNbt);
            SDMEconomyService.LOGGER.info("Accepted synchronization packet for Account!");
        } catch (Exception e) {
            SDMEconomyService.LOGGER.error("When accept synchronization packet for Account!", e);
        }
    }
}
