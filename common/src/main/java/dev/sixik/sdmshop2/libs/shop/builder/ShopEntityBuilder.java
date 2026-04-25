package dev.sixik.sdmshop2.libs.shop.builder;

import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

/**
 * Интерфейс для создания сущностей магазина (самого магазина или предложений).
 * @param <T> Тип билдера для реализации цепочки вызовов (Fluent API).
 */
public interface ShopEntityBuilder<T> {

    /**
     * Возвращает список компонентов сущности.
     * @return Список компонентов.
     */
    ObjectArrayList<ShopComponent> getComponents();

    /**
     * Добавляет готовый компонент к сущности.
     * @param component Компонент для добавления.
     * @return Текущий экземпляр билдера.
     */
    default T addComponent(ShopComponent component) {
        getComponents().add(component);
        return (T) this;
    }

    /**
     * Создает и добавляет компонент по его строковому ID.
     * @param inputId ID типа компонента (например, "sdm:item_cost" или просто "item_cost").
     * @param args Аргументы для создания компонента.
     * @return Текущий экземпляр билдера.
     * @throws IllegalArgumentException Если тип компонента не найден.
     */
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
