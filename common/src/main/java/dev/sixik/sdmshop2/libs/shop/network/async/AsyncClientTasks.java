package dev.sixik.sdmshop2.libs.shop.network.async;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;

public class AsyncClientTasks {

    public static void init() {
        AsyncBridge.initClient();
        BlobTransfer.initClient();

        AsyncBridge.registerHandler(AsyncServerTasks.SEND_SHOP_DATA, buf -> {

            final var shop = ShopInstance.fromNetwork(buf);
            EconomyTest.save(shop);

            return null;
        });
    }
}
