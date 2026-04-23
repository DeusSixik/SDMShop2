package dev.sixik.sdmshop2.libs.shop.base.limiter;

import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Базовая единица хранения лимита.
 * Представляет счетчик покупок для конкретного товара (сущности).
 * Использует потокобезопасный {@link AtomicInteger} для счетчика, что позволяет
 * безопасно изменять лимиты при конкурентных запросах.
 */
public class ShopLimiterOfferData {

    /**
     * UUID товара, к которому относится данный лимит
     */
    @Getter
    private final UUID offerId;

    /**
     * Потокобезопасный счетчик количества совершенных покупок
     */
    @Getter
    private final AtomicInteger count;

    /**
     *  Хранит время последней покупки в миллисекундах (Unix Epoch)
     */
    @Getter
    private final AtomicLong lastPurchaseTime;

    @Setter
    private ShopLimiterUpdate update = () -> {};

    public ShopLimiterOfferData(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readInt(), buf.readLong());
    }

    public ShopLimiterOfferData(JsonObject jsonObject) {
        this(
                UUID.fromString(jsonObject.get("entityId").getAsString()),
                jsonObject.has("count") ? jsonObject.get("count").getAsInt() : 0,
                jsonObject.has("lastPurchaseTime") ? jsonObject.get("lastPurchaseTime").getAsLong() : 0L
        );
    }

    public ShopLimiterOfferData(UUID entityId) {
        this(entityId, 0, 0);
    }

    public ShopLimiterOfferData(UUID entityId, int count) {
        this(entityId, count, 0);
    }

    public ShopLimiterOfferData(UUID entityId, int count, long lastPurchaseTime) {
        this.offerId = entityId;
        this.count = new AtomicInteger(count);
        this.lastPurchaseTime = new AtomicLong(lastPurchaseTime);
    }

    /**
     * Вызывать при успешной покупке
     */
    public void markPurchased() {
        markPurchased(System.currentTimeMillis());
    }

    public void markPurchased(long time) {
        this.lastPurchaseTime.set(time);
        update.onUpdate();
    }

    /**
     * Атомарно прибавляет указанное значение к счетчику.
     *
     * @param amount Значение для прибавления
     * @return Новое (обновленное) значение счетчика
     */
    public int add(int amount) {
        int i = count.addAndGet(amount);
        markPurchased();
        return i;
    }

    /**
     * Атомарно вычитает указанное значение из счетчика.
     *
     * @param amount Значение для вычитания
     * @return Новое (обновленное) значение счетчика
     */
    public int minus(int amount) {
        int i = count.addAndGet(-amount);
        update.onUpdate();
        return i;
    }

    /**
     * Атомарно вычитает значение из счетчика, гарантируя,
     * что результат не опустится ниже нуля.
     *
     * @param amount Значение для вычитания
     * @return Новое значение счетчика (>= 0)
     */
    public int safeMinus(int amount) {
        int i = count.updateAndGet(current -> Math.max(0, current - amount));
        update.onUpdate();
        return i;
    }

    /**
     * Атомарно устанавливает новое значение счетчика.
     *
     * @param newValue Новое значение
     */
    public void set(int newValue) {
        count.set(newValue);
        update.onUpdate();
    }

    public int get() {
        return count.get();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("entityId", offerId.toString());
        json.addProperty("count", count.get());
        json.addProperty("lastPurchaseTime", lastPurchaseTime.get());
        return json;
    }

    public void fromJson(JsonObject json) {
        count.set(json.has("count") ? json.get("count").getAsInt() : 0);
        lastPurchaseTime.set(json.has("lastPurchaseTime") ? json.get("lastPurchaseTime").getAsLong() : 0L);
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(offerId);
        buf.writeInt(count.get());
        buf.writeLong(lastPurchaseTime.get());
    }
}
