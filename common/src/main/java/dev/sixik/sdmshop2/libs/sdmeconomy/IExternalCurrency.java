package dev.sixik.sdmshop2.libs.sdmeconomy;

import dev.sixik.sdmshop2.libs.sdmeconomy.icons.CurrencyIcon;
import dev.sixik.sdmshop2.libs.sdmeconomy.icons.IconType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.math.BigDecimal;

/**
 * Позволят создавать валюту которпя применяет какие-то физические объекты (Предметы, жидкости, опыт и т.п)
 */
public interface IExternalCurrency extends ICurrency {

    default boolean withdraw(ServerPlayer player, BigDecimal amount) {
        return withdraw(player, amount, false);
    }

    boolean withdraw(ServerPlayer player, BigDecimal amount, boolean simulate);

    default boolean deposit(ServerPlayer player, BigDecimal amount) {
        return deposit(player, amount, false);
    }

    boolean deposit(ServerPlayer player, BigDecimal amount, boolean simulate);

    BigDecimal getBalance(Player player);

    /**
     * Иконка валюты при рендере
     */
    default CurrencyIcon getIcon() {
        return new CurrencyIcon(IconType.NONE, null);
    }
}
