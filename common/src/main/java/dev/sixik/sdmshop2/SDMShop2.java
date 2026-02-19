package dev.sixik.sdmshop2;

import com.mojang.logging.LogUtils;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.sixik.sdmshop2.libs.platform.SDMPlatform;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
import dev.sixik.sdmshop2.libs.sdmeconomy.commands.SDMEconomyCommands;
import dev.sixik.sdmshop2.libs.sdmeconomy.network.SDMEconomyNetwork;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.commands.SDMShopCommands;
import dev.sixik.sdmshop2.libs.shop.network.SDMShopNetwork;
import dev.sixik.sdmshop2.libs.shop.register.ShopRegister;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;
import org.slf4j.Logger;

public final class SDMShop2 {
    public static final String MODID = "sdmshop2";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        EconomyTest.init();

        SDMPlatform.addReloading(SDMEconomyPlatform::onReload);
        SDMPlatform.addReloading(() -> ShopTable.Instance.reload());

        SDMEconomyPlatform.loadConfigDir(Platform.getConfigFolder());

        SDMEconomyNetwork.init();

        LifecycleEvent.SERVER_BEFORE_START.register(server -> {
            SDMEconomyPlatform.onServerStart(server);
            ShopTable.Instance = new ShopTable(server);
        });

        LifecycleEvent.SERVER_STOPPED.register((server -> {
            SDMEconomyPlatform.onServerStop(server);
            ShopTable.Instance.saveAll();
        }));

        PlayerEvent.PLAYER_JOIN.register(SDMEconomyPlatform::onPlayerJoin);
        PlayerEvent.PLAYER_QUIT.register(SDMEconomyPlatform::onPlayerLeft);

        SDMEconomyPlatform.init();
        ShopRegister.init();

        SDMShopNetwork.init();

        CommandRegistrationEvent.EVENT.register((s1, s2, s3) -> {
            SDMEconomyCommands.registerCommands(s1, s2, s3);
            SDMShopCommands.registerCommands(s1, s2, s3);
        });
    }
}
