package dev.sixik.sdmshop2.libs.shop.scripting.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.sixik.sdmshop2.libs.shop.SDMShopPlatform;

public class ShopKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void init() {
        SDMShopPlatform.invokeKubeJSEvent = () -> JSShopEvents.REGISTER.post(new ShopScriptKubeEvent());
    }

    @Override
    public void registerEvents() {
        JSShopEvents.GROUP.register();
    }
}
