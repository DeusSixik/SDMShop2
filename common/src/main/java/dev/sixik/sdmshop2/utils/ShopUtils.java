package dev.sixik.sdmshop2.utils;

import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ShopUtils {

    public static Map<ResourceLocation, ShopComponent> createDefaultComponentsMap(Class<? extends ShopComponent> include) {
        Map<ResourceLocation, ShopComponent> componentMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, IComponentType<?>> entry : ShopComponentRegistry.getTypes().entrySet()) {
            ShopComponent component = entry.getValue().createDefault();
            if(!include.isInstance(component)) continue;

            componentMap.put(entry.getKey(), component);
        }
        return componentMap;
    }

    public static Map<ResourceLocation, ShopComponent> createDefaultComponentsMap() {
        Map<ResourceLocation, ShopComponent> componentMap = new HashMap<>();
        for (Map.Entry<ResourceLocation, IComponentType<?>> entry : ShopComponentRegistry.getTypes().entrySet()) {
            componentMap.put(entry.getKey(), entry.getValue().createDefault());
        }
        return componentMap;
    }

    public static Optional<ShopComponent> createDefaultComponent(ResourceLocation typeId) {
        return ShopComponentRegistry.getType(typeId).map(IComponentType::createDefault);
    }

    public static <T extends ShopComponent> T createDefaultComponent(IComponentType<T> type) {
        return type.createDefault();
    }
}
