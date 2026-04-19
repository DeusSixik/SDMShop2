package dev.sixik.sdmshop2.libs.shop.network;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncBridge;
import dev.sixik.sdmshop2.libs.shop.network.async.AsyncServerTasks;
import dev.sixik.sdmshop2.utils.ShopUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import java.util.Arrays;
import java.util.function.Function;

public class ShopNetworkManager {

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
}
