package dev.sixik.sdmshop2.libs.shop.base.limiter;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Контейнер персональных данных лимитов для конкретного игрока.
 * Хранит ассоциативный массив (Map), где ключом является UUID товара,
 * а значением — данные о количестве покупок этого товара данным игроком.
 */
public class ShopLimiterPlayerData {

    @Getter
    private final UUID userId;

    private final Map<UUID, ShopLimiterEntityData> dataMap;

    public ShopLimiterPlayerData(UUID userId, FriendlyByteBuf buf) {
        this.userId = userId;
        int size = buf.readVarInt();
        dataMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            dataMap.put(buf.readUUID(), new ShopLimiterEntityData(buf));
        }
    }

    public ShopLimiterPlayerData(JsonObject jsonObject) {
        this(UUID.fromString(jsonObject.get("userId").getAsString()));
        fromJson(jsonObject.get("data").getAsJsonObject());
    }

    public ShopLimiterPlayerData(UUID userId) {
        this.userId = userId;
        this.dataMap = new ConcurrentHashMap<>();
    }

    public boolean add(UUID entityId) {
        return add(entityId, 0);
    }

    public boolean add(UUID entityId, int count) {
        return dataMap.putIfAbsent(entityId, new ShopLimiterEntityData(entityId, count)) == null;
    }

    public boolean remove(UUID entityId) {
       return dataMap.remove(entityId) != null;
    }

    public ShopLimiterEntityData getData(UUID entityId) {
        return dataMap.computeIfAbsent(entityId, ShopLimiterEntityData::new);
    }

    /**
     * Увеличивает счетчик покупок указанного товара на заданное значение
     * и возвращает текущее значение (ДО прибавления).
     *
     * @param userId UUID товара (обратите внимание: в вашем коде переменная названа userId, но логически это entityId товара)
     * @param count Количество добавляемых покупок
     * @return Количество покупок до выполнения операции
     */
    public int getAndUpdate(UUID userId, int count) {
        return dataMap.computeIfAbsent(userId, ShopLimiterEntityData::new).getCount().getAndUpdate(i -> i + count);
    }

    public int getAndAdd(UUID userId, int count) {
        return dataMap.computeIfAbsent(userId, ShopLimiterEntityData::new).getCount().getAndAdd(count);
    }

    /**
     * Возвращает текущее количество покупок для указанного товара.
     *
     * @param userId UUID товара
     * @return Текущее количество покупок
     */
    public int get(UUID userId) {
        return dataMap.computeIfAbsent(userId, ShopLimiterEntityData::new).getCount().get();
    }

    public void clear() {
        dataMap.clear();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        dataMap.forEach((entityId, data) -> json.add(entityId.toString(), data.toJson()));
        return json;
    }

    public void fromJson(JsonObject json) {
        clear();
        json.entrySet().forEach(entry -> dataMap.put(UUID.fromString(entry.getKey()), new ShopLimiterEntityData(entry.getValue().getAsJsonObject())));
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(dataMap.size());
        dataMap.forEach((entityId, data) -> data.toNetwork(buf));
    }
}
