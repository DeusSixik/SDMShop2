package dev.sixik.sdmshop2.libs.sdmeconomy;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.math.BigDecimal;

public interface ICurrency {

    ResourceLocation getId();

    Component getDisplayName();

    String format(BigDecimal decimal);

    int getColor();

}
