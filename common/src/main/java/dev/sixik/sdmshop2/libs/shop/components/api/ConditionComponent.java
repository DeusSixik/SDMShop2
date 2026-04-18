package dev.sixik.sdmshop2.libs.shop.components.api;

import net.minecraft.world.entity.player.Player;

/**
 * Абстрактный компонент, представляющий условие.
 * Используется для проверки возможности выполнения действия игроком (например, доступ к покупке).
 */
public abstract class ConditionComponent extends ShopComponent {

    /**
     * Проверяет, выполняется ли данное условие для указанного игрока.
     *
     * @param player Игрок для проверки
     * @return true, если условие выполнено, иначе false
     */
    public abstract boolean isChecked(Player player);
}
