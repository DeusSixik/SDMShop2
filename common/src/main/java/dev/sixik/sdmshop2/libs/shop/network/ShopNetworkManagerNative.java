package dev.sixik.sdmshop2.libs.shop.network;

import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.base.ObjectIdGetter;
import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.client.SDMShopClient;
import dev.sixik.sdmshop2.libs.shop.components.api.*;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncBridge;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncClientTasks;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncServerTasks;
import dev.sixik.sdmshop2.utils.ShopUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

class ShopNetworkManagerNative {

    public static void sendNewComponent(ShopInstance shop, ShopEntity entity, ShopComponent newComponent, ServerPlayer... players) {
        sendNewComponent(shop, entity, newComponent, Arrays.asList(players));
    }

    public static void sendNewComponent(ShopInstance shop, ShopEntity entity, ShopComponent newComponent, Iterable<ServerPlayer> players) {
        if(!(entity instanceof ObjectIdGetter idGetter)) {
            SDMShop2.LOGGER.error("Entity {} is not instance of ObjectIdGetter", entity.getClass().getName());
            return;
        }

        final UUID uuid = idGetter.getUUID();
        for (ServerPlayer player : players) {
            AsyncBridge.askPlayer(player, AsyncServerTasks.SEND_GET_CLIENT_SHOP_ID, buf -> {
                buf.writeResourceLocation(shop.getId());
                return buf;
            }).thenAcceptAsync(response -> {
                if (!response.isReadable()) {
                    return;
                }

                final boolean equal = response.readBoolean();
                if(!equal) return;

                AsyncBridge.askPlayer(player, AsyncServerTasks.SEND_NEW_COMPONENT_DATA_TO_CLIENT, buf -> {
                    buf.writeUUID(uuid);
                    ShopComponentRegistry.toNetwork(buf, newComponent);
                    return buf;
                });
            });
        }
    }

    public static void sendShopDataAndOpen(ShopInstance shop, ServerPlayer... players) {
        sendShopDataAndOpen(shop, Arrays.asList(players));
    }

    public static void sendShopDataAndOpen(ShopInstance shop, Iterable<ServerPlayer> players) {
        broadcast(players, AsyncServerTasks.SEND_SHOP_DATA_AND_OPEN, buf -> {
            shop.serializeNetwork(buf);
            return buf;
        });
    }

    public static void sendShopData(ShopInstance shop, ServerPlayer... players) {
        sendShopData(shop, Arrays.asList(players));
    }

    public static void sendShopData(ShopInstance shop, Iterable<ServerPlayer> players) {
        broadcast(players, AsyncServerTasks.SEND_SHOP_DATA, buf -> {
            shop.serializeNetwork(buf);
            return buf;
        });
    }

    public static void sendLimiterData(ServerPlayer... players) {
        sendLimiterData(Arrays.asList(players));
    }

    public static void sendLimiterData(Iterable<ServerPlayer> players) {
        ShopUtils.getLimiterTable(false).ifPresent(limiterTable -> {
            for (ServerPlayer player : players) {
                AsyncBridge.askPlayer(player, AsyncServerTasks.SEND_SHOP_LIMITER_DATA, buf -> {
                    limiterTable.toNetwork(player.getGameProfile().getId(), buf);
                    return buf;
                });
            }
        });
    }

    public static void broadcast(Iterable<ServerPlayer> players, String task, Function<FriendlyByteBuf, FriendlyByteBuf> writer) {
        for (ServerPlayer player : players) {
            AsyncBridge.askPlayer(player, task, writer);
        }
    }

    /**
     * Запросить цену для одного товара по конкретной группе оплаты
     */
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<Map<CostComponent, Double>> getOfferPrice(ShopOffer shopOffer, @Nullable String chosenGroupId) {
        return AsyncBridge.askServer(AsyncClientTasks.GET_PRICES_FOR_OFFER, buf -> {
            buf.writeResourceLocation(SDMShopClient.Shop.getId());
            buf.writeBoolean(false); // isBatch = false
            buf.writeUtf(chosenGroupId != null ? chosenGroupId : "");
            buf.writeUUID(shopOffer.getUUID());
            return buf;
        }).thenApply((response) -> {
            if (!response.isReadable()) {
                SDMShop2.LOGGER.warn("[ShopPriceClientApi] Received empty response from server!");
                return Collections.emptyMap();
            }

            UUID offerUUID = response.readUUID();
            ShopOffer localOffer = SDMShopClient.Shop.getEntries().getEntry(offerUUID);
            List<CostComponent> localCosts = localOffer != null ? localOffer.getComponents(CostComponent.class) : Collections.emptyList();

            return response.readMap(
                    buf -> {
                        int index = buf.readVarInt();
                        if (index < 0 || index >= localCosts.size()) {
                            SDMShop2.LOGGER.error("[ShopPriceClientApi] Desync! Component index {} out of bounds for offer {}.", index, offerUUID);
                            return null;
                        }
                        return localCosts.get(index);
                    },
                    FriendlyByteBuf::readDouble
            );
        });
    }

    /**
     * Запросить цены для списка товаров (например, при открытии страницы магазина)
     */
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<Map<UUID, Map<CostComponent, Double>>> getOffersPrice(Collection<ShopOffer> shopOffers, @Nullable String chosenGroupId) {
        return AsyncBridge.askServer(AsyncClientTasks.GET_PRICES_FOR_OFFER, buf -> {
            buf.writeResourceLocation(SDMShopClient.Shop.getId());
            buf.writeBoolean(true); // isBatch = true
            buf.writeUtf(chosenGroupId != null ? chosenGroupId : "");
            buf.writeVarInt(shopOffers.size());
            for (ShopOffer offer : shopOffers) {
                buf.writeUUID(offer.getUUID());
            }
            return buf;
        }).thenApply((response) -> {
            if (!response.isReadable()) {
                SDMShop2.LOGGER.warn("[ShopPriceClientApi] Received empty response from server!");
                return Collections.emptyMap();
            }

            Map<UUID, Map<CostComponent, Double>> result = new HashMap<>();
            int responseSize = response.readVarInt();
            for (int i = 0; i < responseSize; i++) {
                if (!response.isReadable()) {
                    SDMShop2.LOGGER.error("[ShopPriceClientApi] Malformed packet! Expected {} offers, but buffer ran out of bytes at index {}.", responseSize, i);
                    break;
                }

                UUID offerUUID = response.readUUID();
                ShopOffer localOffer = SDMShopClient.Shop.getEntries().getEntry(offerUUID);
                List<CostComponent> localCosts = localOffer != null ? localOffer.getComponents(CostComponent.class) : Collections.emptyList();

                Map<CostComponent, Double> prices = response.readMap(
                        buf -> {
                            int index = buf.readVarInt();
                            if (index < 0 || index >= localCosts.size()) {
                                SDMShop2.LOGGER.error("[ShopPriceClientApi] Desync! Component index {} out of bounds for offer {}.", index, offerUUID);
                                return null;
                            }
                            return localCosts.get(index);
                        },
                        FriendlyByteBuf::readDouble
                );
                result.put(offerUUID, prices);
            }
            if (response.isReadable()) {
                SDMShop2.LOGGER.warn("[ShopPriceClientApi] Packet had {} unread bytes left over!", response.readableBytes());
            }

            return result;
        });
    }

    /**
     * Запросить серверные условия для одного товара
     */
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<Map<ConditionComponent, Boolean>> fetchServerCondition(ShopOffer shopOffer) {
        return AsyncBridge.askServer(AsyncClientTasks.GET_CONDITIONS_FOR_OFFER, buf -> {
            buf.writeResourceLocation(SDMShopClient.Shop.getId());
            buf.writeBoolean(false); // isBatch = false
            buf.writeUUID(shopOffer.getUUID());
            return buf;
        }).thenApply((response) -> {
            if (!response.isReadable()) {
                SDMShop2.LOGGER.warn("[ShopConditionClientApi] Received empty response from server!");
                return Collections.emptyMap();
            }

            UUID offerUUID = response.readUUID();
            ShopOffer localOffer = SDMShopClient.Shop.getEntries().getEntry(offerUUID);
            List<ConditionComponent> localConditions = localOffer != null ? localOffer.getComponents(ConditionComponent.class) : Collections.emptyList();

            return response.readMap(
                    buf -> {
                        int index = buf.readVarInt();
                        if (index < 0 || index >= localConditions.size()) {
                            SDMShop2.LOGGER.error("[ShopConditionClientApi] Desync! Component index {} out of bounds for offer {}.", index, offerUUID);
                            return null;
                        }
                        return localConditions.get(index);
                    },
                    FriendlyByteBuf::readBoolean
            );
        });
    }

    /**
     * Запросить серверные условия для списка товаров (батч)
     */
    @Environment(EnvType.CLIENT)
    public static CompletableFuture<Map<UUID, Map<ConditionComponent, Boolean>>> fetchServerConditions(Collection<ShopOffer> shopOffers) {
        return AsyncBridge.askServer(AsyncClientTasks.GET_CONDITIONS_FOR_OFFER, buf -> {
            buf.writeResourceLocation(SDMShopClient.Shop.getId());
            buf.writeBoolean(true); // isBatch = true
            buf.writeVarInt(shopOffers.size());
            for (ShopOffer offer : shopOffers) {
                buf.writeUUID(offer.getUUID());
            }
            return buf;
        }).thenApply((response) -> {
            if (!response.isReadable()) {
                SDMShop2.LOGGER.warn("[ShopConditionClientApi] Received empty response from server!");
                return Collections.emptyMap();
            }

            Map<UUID, Map<ConditionComponent, Boolean>> result = new HashMap<>();
            int responseSize = response.readVarInt();
            for (int i = 0; i < responseSize; i++) {
                if (!response.isReadable()) {
                    SDMShop2.LOGGER.error("[ShopConditionClientApi] Malformed packet! Expected {} offers, but buffer ran out of bytes at index {}.", responseSize, i);
                    break;
                }

                UUID offerUUID = response.readUUID();
                ShopOffer localOffer = SDMShopClient.Shop.getEntries().getEntry(offerUUID);

                List<ConditionComponent> localConditions = localOffer != null ? localOffer.getComponents(ConditionComponent.class) : Collections.emptyList();

                Map<ConditionComponent, Boolean> conditions = response.readMap(
                        buf -> {
                            int index = buf.readVarInt();
                            if (index < 0 || index >= localConditions.size()) {
                                SDMShop2.LOGGER.error("[ShopConditionClientApi] Desync! Component index {} out of bounds for offer {}.", index, offerUUID);
                                return null;
                            }
                            return localConditions.get(index);
                        },
                        FriendlyByteBuf::readBoolean
                );
                result.put(offerUUID, conditions);
            }

            if (response.isReadable()) {
                SDMShop2.LOGGER.warn("[ShopConditionClientApi] Packet had {} unread bytes left over!", response.readableBytes());
            }

            return result;
        });
    }
}
