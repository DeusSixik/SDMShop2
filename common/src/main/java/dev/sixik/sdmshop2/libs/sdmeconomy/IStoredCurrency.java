package dev.sixik.sdmshop2.libs.sdmeconomy;

import java.math.BigDecimal;

/**
 * Позволяет создать валюту которая не будет физическая. То есть у неё нет физического предмета или т.п <br>
 * Если нужно создать валюту которая подкреплена физическим объектом то смотрите {@link IExternalCurrency}
 */
public interface IStoredCurrency extends ICurrency {

    BigDecimal getDefaultBalance();
}
