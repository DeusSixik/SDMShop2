package dev.sixik.sdmshop2.libs.shop.builder;

import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public interface ShopEntityBuilder<T> {

    ObjectArrayList<ShopComponent> getComponents();

    default T addComponent(ShopComponent component) {
        getComponents().add(component);
        return (T) this;
    }

    default T addComponent(String inputId, Object... args) {
        ResourceLocation id = ResourceLocation.tryParse(inputId);
        if (id == null)
            id = ResourceLocation.tryBuild("sdm", inputId);

        final Optional<IComponentType<?>> opt = ShopComponentRegistry.getType(id);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Unknown component type: " + id);
        }

        getComponents().add(opt.get().createFromBuilder(args));
        return (T) this;
    }
}
