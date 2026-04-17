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

    /**
     * Атомарно прибавляет указанное значение к счетчику.
     *
     * @param amount Значение для прибавления
     * @return Новое (обновленное) значение счетчика
     */
    public int add(int amount) {
        return count.addAndGet(amount);
    }

    /**
     * Атомарно вычитает указанное значение из счетчика.
     *
     * @param amount Значение для вычитания
     * @return Новое (обновленное) значение счетчика
     */
    public int minus(int amount) {
        return count.addAndGet(-amount);
    }

    /**
     * Атомарно вычитает значение из счетчика, гарантируя,
     * что результат не опустится ниже нуля.
     *
     * @param amount Значение для вычитания
     * @return Новое значение счетчика (>= 0)
     */
    public int safeMinus(int amount) {
        return count.updateAndGet(current -> Math.max(0, current - amount));
    }

    /**
     * Атомарно устанавливает новое значение счетчика.
     *
     * @param newValue Новое значение
     */
    public void set(int newValue) {
        count.set(newValue);
    }

    public int get() {
        return count.get();
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
