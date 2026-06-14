package dev.sixik.sdmshop2.libs.shop.register;

import dev.sixik.sdmshop2.libs.shop.components.CommandRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.ItemRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.conditions.CooldownConditionComponent;
import dev.sixik.sdmshop2.libs.shop.components.limiter.LimiterComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.*;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import dev.sixik.sdmshop2.libs.shop.components.money.MoneyCostComponent;
import dev.sixik.sdmshop2.libs.shop.components.money.MoneyRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.promo.conditions.PromoCooldownComponent;
import dev.sixik.sdmshop2.libs.shop.components.promo.effects.DiscountComponent;
import dev.sixik.sdmshop2.libs.shop.scripting.ScriptConditionComponent;
import dev.sixik.sdmshop2.libs.shop.scripting.ScriptRewardComponent;

public class ShopRegister {

    public static void init() {
        initComponents();
    }

    private static void initComponents() {
        ShopComponentRegistry.register(MoneyCostComponent.TYPE);
        ShopComponentRegistry.register(MoneyRewardComponent.TYPE);
        ShopComponentRegistry.register(CatalogComponent.TYPE);
        ShopComponentRegistry.register(ShopOffersContainerComponent.TYPE);
        ShopComponentRegistry.register(ShopCategoriesContainerComponent.TYPE);
        ShopComponentRegistry.register(ItemRewardComponent.TYPE);
        ShopComponentRegistry.register(CommandRewardComponent.TYPE);
        ShopComponentRegistry.register(RenderHideComponent.TYPE);
        ShopComponentRegistry.register(LimiterComponent.TYPE);
        ShopComponentRegistry.register(DiscountComponent.TYPE);
        ShopComponentRegistry.register(PromoCooldownComponent.TYPE);
        ShopComponentRegistry.register(CooldownConditionComponent.TYPE);
        ShopComponentRegistry.register(NameComponent.TYPE);

        ShopComponentRegistry.register(ScriptRewardComponent.TYPE);
        ShopComponentRegistry.register(ScriptConditionComponent.TYPE);
    }
}
