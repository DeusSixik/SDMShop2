package dev.sixik.sdmshop2.libs.sdmeconomy;

import dev.sixik.sdmshop2.libs.sdmeconomy.custom_currency.BasicCoinCurrency;
import net.minecraft.resources.ResourceLocation;

public class DynamicStoredCurrency extends BasicCoinCurrency {

    protected ResourceLocation id;

    public DynamicStoredCurrency(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    public DynamicStoredCurrency setId(ResourceLocation id) {
        this.id = id;
        return this;
    }
}
