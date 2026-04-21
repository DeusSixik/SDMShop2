package dev.sixik.sdmshop2.libs.shop.client;

import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTableClient;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import org.jetbrains.annotations.NotNull;

public class SDMShopClientEvents {

    public interface AcceptShopEvent {

        void onAcceptShopEvent(@NotNull ShopInstance shop);
    }

    @Deprecated
    public interface ShopDataInvalidateEvent {

        void onDataInvalidateEvent();
    }

    public interface AcceptLimiterDataEvent {

        void onAcceptLimiterDataEvent(@NotNull ShopLimiterTableClient data);
    }

    public interface AcceptNewComponentDataEvent {

        void onAcceptNewComponentDataEvent(@NotNull ShopInstance shop, @NotNull ShopEntity entity, @NotNull ShopComponent newComponent);
    }
}
