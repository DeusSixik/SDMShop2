package dev.sixik.sdmshop2;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.sixik.sdmshop2.libs.platform.SDMPlatform;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
import dev.sixik.sdmshop2.libs.sdmeconomy.commands.SDMEconomyCommands;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTableServer;
import dev.sixik.sdmshop2.libs.shop.commands.SDMShopCommands;
import dev.sixik.sdmshop2.libs.shop.config.ShopConfig;
import dev.sixik.sdmshop2.libs.shop.network.SDMShopNetwork;
import dev.sixik.sdmshop2.libs.shop.register.ShopRegister;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;
import org.slf4j.Logger;

public final class SDMShop2 {
    public static final String MODID = "sdmshop2";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static final ShopTable.Manager SHOP_TABLE_MANAGER = new ShopTable.Manager();
    private static final ShopLimiterTableServer.Manager SHOP_LIMITER_TABLE_MANAGER = new ShopLimiterTableServer.Manager();

    public static void init() {
        EconomyTest.init();

        SDMPlatform.addTask(SHOP_TABLE_MANAGER);
        SDMPlatform.addTask(SHOP_LIMITER_TABLE_MANAGER);

        SDMEconomyPlatform.init();
        ShopRegister.init();

        SDMShopNetwork.init();

        CommandRegistrationEvent.EVENT.register((s1, s2, s3) -> {
            SDMEconomyCommands.registerCommands(s1, s2, s3);
            SDMShopCommands.registerCommands(s1, s2, s3);
        });

        SDMPlatform.init();
    }

    private static final ShopConfig TEMP_CONFIG = new ShopConfig();

    public static ShopConfig getConfig() {
        return TEMP_CONFIG;
    }
}
