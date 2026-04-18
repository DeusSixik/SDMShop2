package dev.sixik.sdmshop2.libs.shop.scripting.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.sixik.sdmshop2.libs.shop.scripting.events.ShopScriptEvents;

public class ShopScriptKubeEvent extends EventJS {

    public void registerConditionEvent(ShopScriptEvents.ScripConditionEvent event) {
        ShopScriptEvents.SCRIP_CONDITION_EVENT.register(event);
    }

    public void registerRewardEvent(ShopScriptEvents.ScripRewardEvent event) {
        ShopScriptEvents.SCRIP_REWARD_EVENT.register(event);
    }

    public void registerPriceEvent(ShopScriptEvents.ScriptCalculatePriceEvent event) {
        ShopScriptEvents.SCRIPT_CALCULATE_PRICE_EVENT.register(event);
    }

    public void registerShopLoadEvent(ShopScriptEvents.ScriptShopLoadEvent event) {
        ShopScriptEvents.SCRIPT_SHOP_LOAD_EVENT.register(event);
    }
}
