package dev.sixik.sdmshop2.libs.shop.register;

import dev.sixik.sdmshop2.libs.shop.components.CategoryComponent;
import dev.sixik.sdmshop2.libs.shop.components.ShopEntriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.ShopCategoriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import dev.sixik.sdmshop2.libs.shop.components.MoneyCostComponent;

public class ShopRegister {

    public static void init() {
        initComponents();
    }

    private static void initComponents() {
        ShopComponentRegistry.register(MoneyCostComponent.TYPE);
        ShopComponentRegistry.register(CategoryComponent.TYPE);
        ShopComponentRegistry.register(ShopEntriesContainerComponent.TYPE);
        ShopComponentRegistry.register(ShopCategoriesContainerComponent.TYPE);
    }
}
