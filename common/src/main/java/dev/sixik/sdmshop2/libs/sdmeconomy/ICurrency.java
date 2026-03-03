package dev.sixik.sdmshop2.libs.sdmeconomy;

import dev.sixik.sdmshop2.libs.sdmeconomy.icons.CurrencyIcon;
import dev.sixik.sdmshop2.libs.sdmeconomy.icons.IconType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;

public interface ICurrency {

    /**
     * Уникальное ID валюты
     */
    ResourceLocation getId();

    /**
     * Отображаемое имя валюты
     */
    Component getDisplayName();

    /**
     * Стороковое обозначение N количества валюты
     */
    String format(BigDecimal decimal);

    /**
     * Цвет текста при отображении
     */
    int getColor();

    /**
     * Иконка валюты при рендере
     */
    default CurrencyIcon getIcon() {
        return CurrencyIcon.EMPTY;
    }
}
