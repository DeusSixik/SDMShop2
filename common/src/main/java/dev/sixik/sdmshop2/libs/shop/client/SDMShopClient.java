package dev.sixik.sdmshop2.libs.shop.client;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncClientTasks;

public class SDMShopClient {

    /**
     * Вызываеться если сервер вдруг изменил данные магазина которые ещё не были отправленны
     */
    @Deprecated
    public static final Event<SDMShopClientEvents.ShopDataInvalidateEvent> SHOP_DATA_INVALIDATE_EVENT =
            EventFactory.createLoop(SDMShopClientEvents.ShopDataInvalidateEvent.class);

    /**
     * Вызываеться когда сервер прислал данные магазина
     */
    public static final Event<SDMShopClientEvents.AcceptShopEvent> ACCEPT_SHOP_EVENT =
            EventFactory.createLoop(SDMShopClientEvents.AcceptShopEvent.class);

    /**
     * Вызываеться когда сервер прислал данные лимитера
     */
    public static final Event<SDMShopClientEvents.AcceptLimiterDataEvent> ACCEPT_LIMITER_DATA_EVENT =
            EventFactory.createLoop(SDMShopClientEvents.AcceptLimiterDataEvent.class);

    /**
     * Вызываеться когда сервер прислал новый компонент для {@link dev.sixik.sdmshop2.libs.shop.base.ShopEntity}
     */
    public static final Event<SDMShopClientEvents.AcceptNewComponentDataEvent> ACCEPT_NEW_COMPONENT_DATA_EVENT =
            EventFactory.createLoop(SDMShopClientEvents.AcceptNewComponentDataEvent.class);

    public static ShopInstance Shop = ShopInstance.createManager(ShopInstance.NULL_MANAGER, false);

    public static void init() {
        AsyncClientTasks.init();
    }

    public static void openShopGui() {
        System.out.println("Open Shop");

        ShopTable.Instance.save(Shop);
    }
}
