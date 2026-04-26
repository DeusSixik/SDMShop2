package dev.sixik.sdmshop2.libs.shop.base;

import dev.sixik.sdmshop2.libs.shop.base.callbacks.ShopEntityCallbacks;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;

public interface ShopEntityCallbackSupport {
    
    @Nullable
    ObjectArrayList<ShopEntityCallbacks.OnAddComponent> getAddComponentListeners();

    @Nullable
    ObjectArrayList<ShopEntityCallbacks.OnRemoveComponent> getRemoveComponentListeners();

    @Nullable
    ObjectArrayList<ShopEntityCallbacks.OnComponentUpdate> getUpdateComponentListeners();

    @Nullable
    ObjectArrayList<ShopEntityCallbacks.OnUpdate> getUpdateListeners();

    void subscribeAddComponent(ShopEntityCallbacks.OnAddComponent callback);

    void subscribeRemoveComponent(ShopEntityCallbacks.OnRemoveComponent callback);

    void subscribeUpdateComponent(ShopEntityCallbacks.OnComponentUpdate callback);

    void subscribeUpdate(ShopEntityCallbacks.OnUpdate callback);

    void unsubscribeAddComponent(ShopEntityCallbacks.OnAddComponent callback);

    void unsubscribeRemoveComponent(ShopEntityCallbacks.OnRemoveComponent callback);

    void unsubscribeUpdateComponent(ShopEntityCallbacks.OnComponentUpdate callback);

    void unsubscribeUpdate(ShopEntityCallbacks.OnUpdate callback);

    default void invokeAddComponent(ShopEntity entity, ShopComponent component) {
        final var list = getAddComponentListeners();
        if (list == null || list.isEmpty()) return;
        final Object[] elements = list.elements();
        for (int i = list.size() - 1; i >= 0; i--) {
            ((ShopEntityCallbacks.OnAddComponent)elements[i]).onComponentAdd(entity, component);
        }
    }    
    
    default void invokeRemoveComponent(ShopEntity entity, ShopComponent component) {
        final var list = getRemoveComponentListeners();
        if (list == null || list.isEmpty()) return;
        final Object[] elements = list.elements();
        for (int i = list.size() - 1; i >= 0; i--) {
            ((ShopEntityCallbacks.OnRemoveComponent)elements[i]).onComponentRemove(entity, component);
        }
    }

    default void invokeUpdateComponent(ShopEntity entity, ShopComponent component) {
        final var list = getUpdateComponentListeners();
        if (list == null || list.isEmpty()) return;
        final Object[] elements = list.elements();
        for (int i = list.size() - 1; i >= 0; i--) {
            ((ShopEntityCallbacks.OnComponentUpdate)elements[i]).onComponentUpdate(entity, component);
        }
    }

    default void invokeUpdate(ShopEntity entity) {
        entity.onUpdate();
        final var list = getUpdateListeners();
        if (list == null || list.isEmpty()) return;
        final Object[] elements = list.elements();
        for (int i = list.size() - 1; i >= 0; i--) {
            ((ShopEntityCallbacks.OnUpdate)elements[i]).onUpdate(entity);
        }
    }


}
