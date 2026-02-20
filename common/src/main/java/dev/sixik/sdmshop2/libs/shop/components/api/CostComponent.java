package dev.sixik.sdmshop2.libs.shop.components.api;

import net.minecraft.world.entity.player.Player;

public abstract class CostComponent extends ShopComponent {

    public abstract boolean canPay(Player player);

    public final void payInternal(Player player) {
        if(canPay(player))
            pay(player);
    }

    public abstract void pay(Player player);
}
