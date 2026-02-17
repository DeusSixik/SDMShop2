package dev.sixik.sdmshop2.tests.economy;

import dev.sixik.sdmshop2.libs.sdmeconomy.IStoredCurrency;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;

public class TestSDMCoin implements IStoredCurrency {

    @Override
    public BigDecimal getDefaultBalance() {
        return BigDecimal.ZERO;
    }

    @Override
    public ResourceLocation getId() {
        return ResourceLocation.tryBuild("sdm", "sdmcoin");
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("SDM Coin");
    }

    @Override
    public String format(BigDecimal decimal) {
        return decimal.toString();
    }

    @Override
    public int getColor() {
        return 0;
    }
}
