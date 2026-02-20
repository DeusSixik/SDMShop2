package dev.sixik.sdmshop2.libs.shop.components.api;

import net.minecraft.world.entity.player.Player;

public abstract class RenderHideComponent extends ShopComponent {

    /**
     * @return true прошёл проверку false нужно скрыть
     */
    public abstract boolean isChecked(Player player);
}
