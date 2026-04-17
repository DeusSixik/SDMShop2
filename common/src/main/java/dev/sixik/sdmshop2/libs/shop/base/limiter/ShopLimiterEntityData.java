package dev.sixik.sdmshop2.libs.shop.base.limiter;

import com.google.gson.JsonObject;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Базовая единица хранения лимита.
 * Представляет счетчик покупок для конкретного товара (сущности).
 * Использует потокобезопасный {@link AtomicInteger} для счетчика, что позволяет
 * безопасно изменять лимиты при конкурентных запросах.
 */
public class ShopLimiterEntityData {

    /**
     * UUID товара, к которому относится данный лимит
     */
    @Getter
    private final UUID entityId;

    /**
     * Потокобезопасный счетчик количества совершенных покупок
     */
    @Getter
    private final AtomicInteger count;

    public ShopLimiterEntityData(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readInt());
    }

    public ShopLimiterEntityData(JsonObject jsonObject) {
        this(UUID.fromString(jsonObject.get("entityId").getAsString()), jsonObject.get("count").getAsInt());
    }

    public ShopLimiterEntityData(UUID entityId) {
        this(entityId, 0);
    }

    public ShopLimiterEntityData(UUID entityId, int count) {
        this.entityId = entityId;
        this.count = new AtomicInteger(count);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("entityId", entityId.toString());
        json.addProperty("count", count.get());
        return json;
    }

    public void fromJson(JsonObject json) {
        count.set(json.get("count").getAsInt());
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(entityId);
        buf.writeInt(count.get());
    }
}
