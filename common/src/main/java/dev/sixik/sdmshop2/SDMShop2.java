package dev.sixik.sdmshop2;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;

public final class SDMShop2 {
    public static final String MOD_ID = "sdmshop2";

    public static void init() {
        SDMEconomyPlatform.loadConfigDir(Platform.getConfigFolder());
        LifecycleEvent.SERVER_BEFORE_START.register(SDMEconomyPlatform::onServerStart);
        LifecycleEvent.SERVER_STOPPED.register(SDMEconomyPlatform::onServerStop);
        PlayerEvent.PLAYER_QUIT.register(SDMEconomyPlatform::onPlayerLeft);
        SDMEconomyPlatform.init();

    }
}
