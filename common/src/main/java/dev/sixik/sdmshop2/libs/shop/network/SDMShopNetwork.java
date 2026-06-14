package dev.sixik.sdmshop2.libs.shop.network;

import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.architectury.platform.Platform;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncClientTasks;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncServerTasks;
import net.fabricmc.api.EnvType;

public class SDMShopNetwork {

    private static final SimpleNetworkManager NET = SimpleNetworkManager.create(SDMShop2.MODID);

    public static void init() {

    }
}
