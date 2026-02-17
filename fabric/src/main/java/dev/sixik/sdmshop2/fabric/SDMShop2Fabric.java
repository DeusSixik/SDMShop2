package dev.sixik.sdmshop2.fabric;

import dev.sixik.sdmshop2.SDMShop2;
import net.fabricmc.api.ModInitializer;

public final class SDMShop2Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        // Run our common setup.
        SDMShop2.init();
    }
}
