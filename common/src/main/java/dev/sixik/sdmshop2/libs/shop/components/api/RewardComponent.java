package dev.sixik.sdmshop2.libs.shop.components.api;

import net.minecraft.server.level.ServerPlayer;

public abstract class RewardComponent extends ShopComponent {

    public abstract void reward(ServerPlayer player);
}
