package dev.sixik.sdmshop2.libs.shop.components.api;

import net.minecraft.world.entity.player.Player;

public abstract class ConditionComponent extends ShopComponent {

    public abstract boolean isChecked(Player player);
}
