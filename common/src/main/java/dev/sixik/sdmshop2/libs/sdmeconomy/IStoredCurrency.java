package dev.sixik.sdmshop2.libs.sdmeconomy;

import java.math.BigDecimal;

public interface IStoredCurrency extends ICurrency {

    BigDecimal getDefaultBalance();
}
