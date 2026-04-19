package dev.sixik.sdmshop2.libs.shop.network.async;

public class AsyncServerTasks {

    public static final String SEND_SHOP_DATA = "send_shop_data";

    public static final String SEND_SHOP_LIMITER_DATA = "send_shop_limiter_data";

    public static void init() {
        AsyncBridge.initServer();
        BlobTransfer.initServer();
    }
}
