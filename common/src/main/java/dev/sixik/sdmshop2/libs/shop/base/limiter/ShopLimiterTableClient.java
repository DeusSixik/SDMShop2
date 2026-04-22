package dev.sixik.sdmshop2.libs.shop.base.limiter;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.client.SDMShopClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Клиентская реализация таблицы лимитов магазина.
 * Хранит глобальные лимиты и персональные лимиты ТОЛЬКО локального игрока (клиента).
 * Методы сохранения и отправки данных на сервер в этой реализации не поддерживаются (бросают UnsupportedOperationException).
 */
public final class ShopLimiterTableClient implements ShopLimiterTable {

    private final Map<UUID, ShopLimiterEntityData> entitiesData = new ConcurrentHashMap<>();
    private ShopLimiterPlayerData localPlayerData;

    public static final ShopLimiterTableClient INSTANCE = new ShopLimiterTableClient();

    public ShopLimiterTableClient() { }

    @Override
    public ShopLimiterEntityData getEntityData(UUID entityId) {
        return entitiesData.computeIfAbsent(entityId, ShopLimiterEntityData::new);
    }

    public ShopLimiterPlayerData getPlayerData() {
        return getPlayerData(Minecraft.getInstance().player.getGameProfile().getId());
    }

    @Override
    public ShopLimiterPlayerData getPlayerData(Player player) {
        return getPlayerData(player);
    }

    /**
     * Возвращает данные локального игрока.
     * * @param playerId UUID игрока. Должен строго совпадать с UUID локального клиента.
     * @return Объект персональных данных локального игрока.
     * @throws IllegalArgumentException если переданный playerId не принадлежит локальному игроку.
     */
    @Override
    public ShopLimiterPlayerData getPlayerData(UUID playerId) {
        if(playerId != null && !playerId.equals(Minecraft.getInstance().player.getGameProfile().getId()))
            throw new IllegalArgumentException("Player ID must be equal to local player ID");

        if (localPlayerData == null) {
            localPlayerData = new ShopLimiterPlayerData(playerId);
        }

        return localPlayerData;
    }

    /**
     * Читает данные, присланные сервером, и обновляет локальный кэш клиента.
     *
     * @param buf Сетевой буфер с данными от сервера
     */
    @Override
    public void fromNetwork(FriendlyByteBuf buf) {
        entitiesData.clear();
        localPlayerData = new ShopLimiterPlayerData(Minecraft.getInstance().player.getGameProfile().getId(), buf);

        int entitiesSize = buf.readVarInt();
        for (int i = 0; i < entitiesSize; i++) {
            ShopLimiterEntityData data = new ShopLimiterEntityData(buf);
            entitiesData.put(data.getEntityId(), data);
        }

        SDMShopClient.ACCEPT_LIMITER_DATA_EVENT.invoker().onAcceptLimiterDataEvent(this);
    }

    @Override
    public void toNetwork(UUID player, FriendlyByteBuf buf) {
        throw new UnsupportedOperationException("Client cannot send full table to server");
    }

    @Override
    public JsonObject toJson() {
        throw new UnsupportedOperationException("Client does not save to JSON");
    }

    @Override
    public void fromJson(JsonObject json) {
        throw new UnsupportedOperationException("Client does not load from JSON");
    }
}
