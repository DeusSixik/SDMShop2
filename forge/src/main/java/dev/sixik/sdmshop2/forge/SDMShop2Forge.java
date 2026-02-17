package dev.sixik.sdmshop2.forge;

import dev.sixik.sdmshop2.SDMShop2;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SDMShop2.MOD_ID)
public final class SDMShop2Forge {
    public SDMShop2Forge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(SDMShop2.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        SDMShop2.init();
    }
}
