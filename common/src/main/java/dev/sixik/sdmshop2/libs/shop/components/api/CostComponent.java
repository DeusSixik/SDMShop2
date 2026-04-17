package dev.sixik.sdmshop2.libs.shop.components.api;

import net.minecraft.world.entity.player.Player;

/**
 * Абстрактный компонент, представляющий стоимость покупки.
 * Отвечает за проверку наличия средств и процесс оплаты.
 */
public abstract class CostComponent extends ShopComponent {

    /**
     * Проверяет, может ли игрок оплатить данную стоимость.
     *
     * @param player Игрок для проверки
     * @return true, если средств достаточно, иначе false
     */
    public abstract boolean canPay(Player player);


    public final void payInternal(Player player) {
        if(canPay(player))
            pay(player);
    }

    /**
     * Списывает стоимость у игрока.
     *
     * @param player Игрок, с которого списываются средства
     */
    public abstract void pay(Player player);
}
