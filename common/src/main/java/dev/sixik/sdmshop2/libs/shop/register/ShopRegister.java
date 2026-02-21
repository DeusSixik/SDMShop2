package dev.sixik.sdmshop2.libs.shop.register;

import dev.sixik.sdmshop2.libs.shop.components.CommandRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.ItemRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.RenderHideComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.CategoryComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.ShopCategoriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.ShopEntriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import dev.sixik.sdmshop2.libs.shop.components.money.MoneyCostComponent;
import dev.sixik.sdmshop2.libs.shop.components.money.MoneyRewardComponent;

public class ShopRegister {

    public static void init() {
        initComponents();
    }

    private static void initComponents() {
        ShopComponentRegistry.register(MoneyCostComponent.TYPE);
        ShopComponentRegistry.register(MoneyRewardComponent.TYPE);
        ShopComponentRegistry.register(CategoryComponent.TYPE);
        ShopComponentRegistry.register(ShopEntriesContainerComponent.TYPE);
        ShopComponentRegistry.register(ShopCategoriesContainerComponent.TYPE);
        ShopComponentRegistry.register(ItemRewardComponent.TYPE);
        ShopComponentRegistry.register(CommandRewardComponent.TYPE);
        ShopComponentRegistry.register(RenderHideComponent.TYPE);
    }
}
