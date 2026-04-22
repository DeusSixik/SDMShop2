package dev.sixik.sdmshop2.libs.shop.scripting.compat.kubejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface JSShopEvents {

    EventGroup GROUP = EventGroup.of("SDMShop");
    EventHandler REGISTER = GROUP.server("register", () -> ShopScriptKubeEvent.class);
}
