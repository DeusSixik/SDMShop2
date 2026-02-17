package dev.sixik.sdmshop2.libs.sdmeconomy.icons;

import org.jetbrains.annotations.Nullable;

/**
 * Holder для хранения данных о инонке
 * @param type Тип (Item, Texture, None)
 * @param icon Иконка в зависимости от типа. Если (None то Null)
 */
public record CurrencyIcon(IconType type, @Nullable Object icon) {
}
