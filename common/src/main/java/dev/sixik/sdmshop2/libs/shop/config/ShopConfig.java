package dev.sixik.sdmshop2.libs.shop.config;

public class ShopConfig {

    public boolean autoSaveShopData = true;
    public int saveShopDataIntervalSeconds = 1800;

    public boolean autoSaveShopLimiterData = true;
    public int saveShopLimiterDataIntervalSeconds = 1800;

    // Позволяет кэшировать данные магазина без неообходимости постоянно их сериалазировать. Повышает потребеление ОЗУ
    public boolean cacheShopNetwork = true;
}
