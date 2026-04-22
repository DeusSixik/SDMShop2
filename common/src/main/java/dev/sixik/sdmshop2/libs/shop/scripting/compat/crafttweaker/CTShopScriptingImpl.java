package dev.sixik.sdmshop2.libs.shop.scripting.compat.crafttweaker;

import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import dev.sixik.sdmshop2.libs.shop.scripting.events.ShopScriptEvents;
import org.openzen.zencode.java.ZenCodeType;

@ZenRegister
@ZenCodeType.Name("mods.sdmshop.scripting.ShopScripting")
public class CTShopScriptingImpl {

    @ZenCodeType.Method
    public static void registerConditionEvent(ShopScriptEvents.ScripConditionEvent event) {
        ShopScriptEvents.SCRIP_CONDITION_EVENT.register(event);
    }

    @ZenCodeType.Method
    public static void registerRewardEvent(ShopScriptEvents.ScripRewardEvent event) {
        ShopScriptEvents.SCRIP_REWARD_EVENT.register(event);
    }

    @ZenCodeType.Method
    public static void registerPriceEvent(ShopScriptEvents.ScriptCalculatePriceEvent event) {
        ShopScriptEvents.SCRIPT_CALCULATE_PRICE_EVENT.register(event);
    }

    @ZenCodeType.Method
    public static void registerShopLoadEvent(ShopScriptEvents.ScriptShopLoadEvent event) {
        ShopScriptEvents.SCRIPT_SHOP_LOAD_EVENT.register(event);
    }
}
