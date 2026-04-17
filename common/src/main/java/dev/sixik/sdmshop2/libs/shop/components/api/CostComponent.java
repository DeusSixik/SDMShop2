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

    /**
     * Безопасно выполняет оплату. Проверяет {@link #canPay(Player)} перед вызовом {@link #pay(Player)}.
     *
     * @param player Игрок, совершающий оплату
     */
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
