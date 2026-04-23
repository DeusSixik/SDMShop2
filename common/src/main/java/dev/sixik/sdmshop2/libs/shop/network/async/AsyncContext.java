package dev.sixik.sdmshop2.libs.shop.network.async;

import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

public record AsyncContext(NetworkManager.PacketContext context, FriendlyByteBuf buf) {
}
