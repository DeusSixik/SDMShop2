package dev.sixik.sdmshop2.libs.shop.client;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncClientTasks;

public class SDMShopClient {

    public static ShopInstance Shop = ShopInstance.createManager(ShopInstance.NULL_MANAGER, false);

    public static void init() {
        AsyncClientTasks.init();
    }

    public static void openShopGui() {

    }
}
