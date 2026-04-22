package dev.sixik.sdmshop2.utils;

import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTable;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTableClient;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTableServer;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ShopUtils {

    public static Optional<ShopLimiterTable> getLimiterTable(boolean isClient) {
        return Optional.ofNullable(isClient ? ShopLimiterTableClient.INSTANCE : ShopLimiterTableServer.getInstance());
    }

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

    public static FriendlyByteBuf createResponse(Consumer<FriendlyByteBuf> writer) {
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        writer.accept(buf);
        return buf;
    }
}
