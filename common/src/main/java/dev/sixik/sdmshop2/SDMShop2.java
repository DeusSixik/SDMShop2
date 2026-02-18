package dev.sixik.sdmshop2;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.sixik.sdmshop2.libs.platform.SDMPlatform;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyCurrencyRegistry;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.register.ShopRegister;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;

public final class SDMShop2 {
    public static final String MOD_ID = "sdmshop2";

    public static void init() {
        EconomyTest.init();

        SDMPlatform.addReloading(SDMEconomyCurrencyRegistry::reload);
        SDMPlatform.addReloading(() -> ShopTable.Instance.reload());

        SDMEconomyPlatform.loadConfigDir(Platform.getConfigFolder());

        LifecycleEvent.SERVER_BEFORE_START.register(server -> {
            SDMEconomyPlatform.onServerStart(server);
            ShopTable.Instance = new ShopTable(server);
        });
        LifecycleEvent.SERVER_STOPPED.register((server -> {

            SDMEconomyPlatform.onServerStop(server);
            ShopTable.Instance.saveAll();
        }));
        PlayerEvent.PLAYER_QUIT.register(SDMEconomyPlatform::onPlayerLeft);
        SDMEconomyPlatform.init();
        ShopRegister.init();
    }
}
