package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.misc.CategoryComponent;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

/**
 * Представляет торговое предложение (товар или услугу) в магазине.
 * Наследует {@link ShopEntity} для управления своими компонентами (цена, предмет, лимиты и т.д.).
 */
public class ShopOffer extends ShopEntity implements ObjectIdGetter {

    /**
     * Создает новый экземпляр торгового предложения.
     *
     * @param uuid                 Уникальный идентификатор предложения
     * @param initializeComponents Нужно ли сразу инициализировать серверные компоненты
     * @return Новый экземпляр ShopOffer
     */
    public static ShopOffer create(UUID uuid, boolean initializeComponents) {
        ShopOffer entry = new ShopOffer(uuid);

        if(initializeComponents)
            entry.initializeServerOnlyComponents();
        return entry;
    }

    /**
     * Уникальный идентификатор предложения.
     */
    @Getter
    private final UUID uuid;

    protected ShopOffer(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("uuid", uuid.toString());
        json.add("components", serializeComponents());
        return json;
    }

    @Override
    public void deserialize(JsonElement json) {
        final JsonObject object = json.getAsJsonObject();

        if(object.has("components"))
            deserializeComponents(object.get("components"));
    }

    @Override
    public void serializeNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        super.serializeNetwork(buf);
    }

    @Override
    protected void customInitializeServerOnlyComponents() {
        addComponent(new CategoryComponent("none"));
    }

    /**
     * Создает торговое предложение из данных сетевого буфера.
     *
     * @param buf Буфер сетевого пакета
     * @return Восстановленный экземпляр ShopOffer
     */
    public static ShopOffer fromNetwork(FriendlyByteBuf buf) {
        final ShopOffer entry = new ShopOffer(buf.readUUID());
        entry.deserializeNetwork(buf);
        return entry;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }
}
