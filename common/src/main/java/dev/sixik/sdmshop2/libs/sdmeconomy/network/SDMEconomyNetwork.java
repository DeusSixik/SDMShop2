package dev.sixik.sdmshop2.libs.sdmeconomy.network;

import dev.architectury.networking.simple.MessageType;
import dev.architectury.networking.simple.SimpleNetworkManager;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
import dev.sixik.sdmshop2.libs.sdmeconomy.network.packets.SendDynamicCurrencyS2C;
import dev.sixik.sdmshop2.libs.sdmeconomy.network.packets.SendPlayerAccountS2C;

public class SDMEconomyNetwork {

    private static final SimpleNetworkManager NET = SimpleNetworkManager.create(SDMEconomyPlatform.MODID);

    public static final MessageType SEND_PLAYER_ACCOUNT = NET.registerS2C("send_player_account", SendPlayerAccountS2C::new);
    public static final MessageType SEND_DYNAMIC_CURRENCIES = NET.registerS2C("send_dynamic_currencies", SendDynamicCurrencyS2C::new);

    public static void init() {

    }

}
