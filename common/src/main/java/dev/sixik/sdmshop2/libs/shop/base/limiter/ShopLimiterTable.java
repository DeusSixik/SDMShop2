package dev.sixik.sdmshop2.libs.shop.base.limiter;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

/**
 * Базовый интерфейс для работы с таблицами лимитов магазина.
 * Управляет данными о количестве покупок как на глобальном уровне (World),
 * так и на персональном (Player).
 */
public interface ShopLimiterTable {

    /**
     * Возвращает данные о глобальных лимитах конкретного товара.
     * Если данные отсутствуют, создает новую запись.
     *
     * @param entityId UUID товара (сущности)
     * @return Объект данных лимита для указанного товара
     */
    ShopLimiterEntityData getEntityData(UUID entityId);

    /**
     * Возвращает персональные данные о лимитах указанного игрока.
     *
     * @param player Объект игрока
     * @return Объект персональных данных лимитов игрока
     */
    ShopLimiterPlayerData getPlayerData(Player player);

    /**
     * Возвращает персональные данные о лимитах игрока по его UUID.
     *
     * @param playerId UUID игрока
     * @return Объект персональных данных лимитов игрока
     */
    ShopLimiterPlayerData getPlayerData(UUID playerId);

    default void save() {
        throw new UnsupportedOperationException();
    }

    default void saveAsync() {
        throw new UnsupportedOperationException();
    }

    default void load() {
        throw new UnsupportedOperationException();
    }

    default void shutdown() { }

    JsonObject toJson();

    void fromJson(JsonObject json);

    void toNetwork(UUID player, FriendlyByteBuf buf);

    void fromNetwork(FriendlyByteBuf buf);
}
