package dev.sixik.sdmshop2.libs.shop.network.async;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;
import dev.sixik.sdmshop2.utils.ShopUtils;
import org.apache.commons.lang3.NotImplementedException;

public class AsyncClientTasks {

    public static void init() {
        AsyncBridge.initClient();
        BlobTransfer.initClient();

        AsyncBridge.registerHandler(AsyncServerTasks.SEND_SHOP_DATA, buf -> {
            throw new NotImplementedException("Shop data accept packet not implemented!");
        });

        AsyncBridge.registerHandler(AsyncServerTasks.SEND_SHOP_LIMITER_DATA, buf -> {
            ShopUtils.getLimiterTable(true).ifPresent(limiterTable -> limiterTable.fromNetwork(buf));
            return null;
        });

    }
}
