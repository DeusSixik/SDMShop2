package dev.sixik.sdmshop2.libs.sdmeconomy.custom_currency;

import dev.sixik.sdmshop2.libs.sdmeconomy.IStoredCurrency;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;

public class BasicCoinCurrency implements IStoredCurrency {

    public static final BasicCoinCurrency CURRENCY = new BasicCoinCurrency();

    public static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "coin");
    public static final Component DISPLAY_NAME = Component.translatable(ID.toString().replace(":", "_"));

    protected BasicCoinCurrency() { }

    @Override
    public BigDecimal getDefaultBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public Component getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public String format(BigDecimal decimal) {
        return String.format("◎ %,f", decimal.doubleValue());
    }

    @Override
    public int getColor() {
        return 0;
    }
}
