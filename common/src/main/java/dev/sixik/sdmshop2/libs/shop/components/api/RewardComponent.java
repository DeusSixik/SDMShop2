package dev.sixik.sdmshop2.libs.shop.components.api;

import net.minecraft.server.level.ServerPlayer;

/**
 * Абстрактный компонент, представляющий награду.
 * Награды выдаются игроку на стороне сервера после успешной покупки.
 */
public abstract class RewardComponent extends ShopComponent {

    /**
     * Выдает награду игроку.
     * Вызывается только на сервере.
     *
     * @param player Игрок, получающий награду
     */
    public abstract void reward(ServerPlayer player);

    /**
     * Награды обычно не должны синхронизироваться с клиентом,
     * так как их логика выполняется исключительно на сервере.
     *
     * @return false по умолчанию
     */
    @Override
    public boolean shouldSync() {
        return false;
    }
}
