package dev.sixik.sdmshop2.libs.shop.network.async;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.components.api.ConditionComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.CostComponent;
import dev.sixik.sdmshop2.libs.shop.processors.ShopTransactionProcessor;
import dev.sixik.sdmshop2.utils.NetworkExtern;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class AsyncServerTasks {

    public static final String SEND_SHOP_DATA = "send_shop_data";
    public static final String SEND_SHOP_DATA_AND_OPEN = "send_shop_data_and_open";

    public static final String SEND_SHOP_LIMITER_DATA = "send_shop_limiter_data";

    public static final String SEND_GET_CLIENT_SHOP_ID = "send_get_client_shop_id";
    public static final String SEND_NEW_COMPONENT_DATA_TO_CLIENT = "send_new_component_data_to_client";

    public static void init() {
        AsyncBridge.initServer();
        BlobTransfer.initServer();

        AsyncBridge.registerHandler(AsyncClientTasks.GET_PRICES_FOR_OFFER, (request, ctx) -> {
            if (!request.isReadable()) return null;

            final ResourceLocation shopId = request.readResourceLocation();
            final boolean isBatch = request.readBoolean();
            final String chosenGroupId = request.readUtf();

            final ShopInstance shopInstance = ShopTable.Instance.getShop(shopId);
            if (shopInstance == null) return null;

            final MinecraftServer server = ShopTable.Instance.getServer();

            FriendlyByteBuf reply = new FriendlyByteBuf(Unpooled.buffer());

            if (isBatch) {
                final int shopOffersCount = request.readVarInt();
                final UUID[] in_data = new UUID[shopOffersCount];
                for (int i = 0; i < shopOffersCount; i++) {
                    in_data[i] = request.readUUID();
                }

                final ShopOffer[] offers = new ShopOffer[shopOffersCount];
                shopInstance.getEntries().getEntries(in_data, offers);

                Map<UUID, Map<CostComponent, Double>> outMap = new HashMap<>();
                for (int i = 0; i < offers.length; i++) {
                    if (offers[i] != null) {
                        outMap.put(in_data[i], ShopTransactionProcessor.calculateFinalCosts(offers[i], server, chosenGroupId));
                    }
                }

                reply.writeVarInt(outMap.size());
                for (int i = 0; i < in_data.length; i++) {
                    UUID offerUuid = in_data[i];
                    ShopOffer offer = offers[i];

                    if (offer == null || !outMap.containsKey(offerUuid)) continue;

                    Map<CostComponent, Double> prices = outMap.get(offerUuid);
                    getPricesForOfferWriteOfferData(reply, offerUuid, offer, prices);
                }
            }
            else {
                final UUID offerId = request.readUUID();
                final ShopOffer offer = shopInstance.getEntries().getEntry(offerId);

                if (offer == null) {
                    reply.release();
                    return null;
                }

                Map<CostComponent, Double> prices = ShopTransactionProcessor
                        .calculateFinalCosts(offer, server, chosenGroupId);
                getPricesForOfferWriteOfferData(reply, offerId, offer, prices);
            }
            return reply;
        });

        AsyncBridge.registerHandler(AsyncClientTasks.GET_CONDITIONS_FOR_OFFER, (request, ctx) -> {
            if (!request.isReadable()) return null;

            final ResourceLocation shopId = request.readResourceLocation();
            final boolean isBatch = request.readBoolean();

            final ShopInstance shopInstance = ShopTable.Instance.getShop(shopId);
            if (shopInstance == null) return null;

            final Player player = ctx.getPlayer();

            if (isBatch) {
                final int shopOffersCount = request.readVarInt();
                final UUID[] in_data = new UUID[shopOffersCount];
                for (int i = 0; i < shopOffersCount; i++) {
                    in_data[i] = request.readUUID();
                }

                final ShopOffer[] offers = new ShopOffer[shopOffersCount];
                shopInstance.getEntries().getEntries(in_data, offers);

                Map<UUID, Map<ConditionComponent, Boolean>> outMap = new HashMap<>();
                for (int i = 0; i < offers.length; i++) {
                    if (offers[i] != null) {
                        Map<ConditionComponent, Boolean> conditionMap = new HashMap<>();
                        final List<ConditionComponent> components = offers[i].getComponents(ConditionComponent.class);
                        for (int j = 0; j < components.size(); j++) {
                            ConditionComponent conditionComponent = components.get(j);
                            if (conditionComponent.verifiedOnClient()) continue;
                            conditionMap.put(conditionComponent, conditionComponent.isChecked(player));
                        }
                        outMap.put(in_data[i], conditionMap);
                    }
                }

                FriendlyByteBuf reply = new FriendlyByteBuf(Unpooled.buffer());
                reply.writeVarInt(outMap.size());

                for (int i = 0; i < in_data.length; i++) {
                    UUID offerUuid = in_data[i];
                    ShopOffer offer = offers[i];

                    if (offer == null || !outMap.containsKey(offerUuid)) continue;

                    Map<ConditionComponent, Boolean> conditionMap = outMap.get(offerUuid);
                    final List<ConditionComponent> components = offer.getComponents(ConditionComponent.class);

                    getConditionForOfferWriteOfferData(offerUuid, conditionMap, components, reply);
                }
                return reply;
            }
            else {
                final UUID offerId = request.readUUID();
                final ShopOffer offer = shopInstance.getEntries().getEntry(offerId);

                if (offer == null) {
                    return null;
                }

                Map<ConditionComponent, Boolean> conditionMap = new HashMap<>();
                final List<ConditionComponent> components = offer.getComponents(ConditionComponent.class);
                for (int i = 0; i < components.size(); i++) {
                    ConditionComponent conditionComponent = components.get(i);
                    if (conditionComponent.verifiedOnClient()) continue;
                    conditionMap.put(conditionComponent, conditionComponent.isChecked(player));
                }

                FriendlyByteBuf reply = new FriendlyByteBuf(Unpooled.buffer());
                getConditionForOfferWriteOfferData(offerId, conditionMap, components, reply);
                return reply;
            }
        });
    }

    private static void getConditionForOfferWriteOfferData(UUID offerId, Map<ConditionComponent, Boolean> conditionMap, List<ConditionComponent> components, FriendlyByteBuf reply) {
        reply.writeUUID(offerId);
        NetworkExtern.writeMap(reply, conditionMap, components, FriendlyByteBuf::writeBoolean);
    }

    private static void getPricesForOfferWriteOfferData(FriendlyByteBuf reply, UUID offerId, ShopOffer offer, Map<CostComponent, Double> prices) {
        reply.writeUUID(offerId);
        NetworkExtern.writeMap(reply, prices, offer.getComponents(CostComponent.class), FriendlyByteBuf::writeDouble);
    }


}
