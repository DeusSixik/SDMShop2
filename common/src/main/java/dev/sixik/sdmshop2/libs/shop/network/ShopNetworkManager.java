package dev.sixik.sdmshop2.libs.shop.network;

import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.components.api.ConditionComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.CostComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ShopNetworkManager {

    public static void sendNewComponent(ShopInstance shop, ShopEntity entity, ShopComponent newComponent, ServerPlayer... players) {
        ShopNetworkManagerNative.sendNewComponent(shop, entity, newComponent, Arrays.asList(players));
    }

    public static void sendNewComponent(ShopInstance shop, ShopEntity entity, ShopComponent newComponent, Iterable<ServerPlayer> players) {
        ShopNetworkManagerNative.sendNewComponent(shop, entity, newComponent, players);
    }

    public static void sendShopDataAndOpen(ShopInstance shop, ServerPlayer... players) {
        ShopNetworkManagerNative.sendShopDataAndOpen(shop, players);
    }

    public static void sendShopDataAndOpen(ShopInstance shop, Iterable<ServerPlayer> players) {
        ShopNetworkManagerNative.sendShopDataAndOpen(shop, players);
    }

    public static void sendShopData(ShopInstance shop, ServerPlayer... players) {
        ShopNetworkManagerNative.sendShopData(shop, players);
    }

    public static void sendShopData(ShopInstance shop, Iterable<ServerPlayer> players) {
        ShopNetworkManagerNative.sendShopData(shop, players);
    }

    public static void sendLimiterData(ServerPlayer... players) {
        ShopNetworkManagerNative.sendLimiterData(players);
    }

    public static void sendLimiterData(Iterable<ServerPlayer> players) {
        ShopNetworkManagerNative.sendLimiterData(players);
    }

    /**
     * Запросить цену для одного товара по конкретной группе оплаты
     */
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<Map<CostComponent, Double>> getOfferPrice(ShopOffer shopOffer, @Nullable String chosenGroupId) {
        return ShopNetworkManagerNative.getOfferPrice(shopOffer, chosenGroupId);
    }

    /**
     * Запросить цены для списка товаров (например, при открытии страницы магазина)
     */
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<Map<UUID, Map<CostComponent, Double>>> getOffersPrice(Collection<ShopOffer> shopOffers, @Nullable String chosenGroupId) {
        return ShopNetworkManagerNative.getOffersPrice(shopOffers, chosenGroupId);
    }

    /**
     * Запросить серверные условия для одного товара
     */
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<Map<ConditionComponent, Boolean>> fetchServerCondition(ShopOffer shopOffer) {
        return ShopNetworkManagerNative.fetchServerCondition(shopOffer);
    }

    /**
     * Запросить серверные условия для списка товаров (батч)
     */
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<Map<UUID, Map<ConditionComponent, Boolean>>> fetchServerConditions(Collection<ShopOffer> shopOffers) {
        return ShopNetworkManagerNative.fetchServerConditions(shopOffers);
    }
}
