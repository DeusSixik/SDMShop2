package dev.sixik.sdmshop2.libs.shop.base.callbacks;

import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;

public interface ShopEntityCallbacks {

    interface OnAddComponent {

        void onComponentAdd(ShopEntity entity, ShopComponent component);
    }

    interface OnRemoveComponent {

        void onComponentRemove(ShopEntity entity, ShopComponent component);
    }

    interface OnUpdate {

        void onUpdate(ShopEntity entity);
    }

    interface OnComponentUpdate {

        void onComponentUpdate(ShopEntity entity, ShopComponent component);
    }
}
