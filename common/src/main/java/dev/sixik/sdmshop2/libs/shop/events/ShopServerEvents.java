package dev.sixik.sdmshop2.libs.shop.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.processors.ShopTransactionProcessor;
import dev.sixik.sdmshop2.libs.shop.scripting.events.ShopScriptEvents;
import net.minecraft.server.MinecraftServer;

public class ShopServerEvents {

    /**
     * Вызываеться когда магазин загружаеться из файла на диске или Базы Данных <br>
     *
     * Вызов данного события происходит после вызова такого же события но для скриптов. {@link ShopScriptEvents#SCRIPT_SHOP_LOAD_EVENT}
     *
     * @see ShopTable#reload()
     */
    public static final Event<ShopScriptEvents.ScriptShopLoadEvent> SHOP_LOAD_EVENT =
            EventFactory.createLoop(ShopScriptEvents.ScriptShopLoadEvent.class);

    /**
     * Вызываеться когда магазин вычисляет цену товара <br>
     * Вызов данного события происходит после вызова такого же события но для скриптов. {@link ShopScriptEvents#SCRIPT_CALCULATE_PRICE_EVENT}
     *
     * @see ShopTransactionProcessor#calculateFinalCosts(ShopOffer, MinecraftServer, String)
     */
    public static final Event<ShopScriptEvents.ScriptCalculatePriceEvent> CALCULATE_PRICE_EVENT =
            EventFactory.createLoop(ShopScriptEvents.ScriptCalculatePriceEvent.class);
}
