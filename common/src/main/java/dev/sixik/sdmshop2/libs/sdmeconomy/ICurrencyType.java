package dev.sixik.sdmshop2.libs.sdmeconomy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public interface ICurrencyType<T extends IExternalCurrency> {

    /**
     * Десериализация валюты из JSON
     * @param id Уникальный идентификатор валюты
     * @param json JSON data
     */
    T deserialize(ResourceLocation id, JsonObject json);

    /**
     * Сериализацию валюты через JSON
     * @param currency Ссылка на валюту
     */
    JsonObject serialize(T currency);

    /**
     * Вспомагательный метод для быстрой записи типа
     * @param object Куда записываем
     * @param type Идентификатор типа
     */
    default void serializeType(JsonObject object, String type) {
        if(type.contains(":")) {
            object.addProperty("type", type);
        } else {
            object.addProperty("type", "sdm:" + type);
        }
    }

    /**
     * Позволяет сериализировать данные через JSON -> NBT
     * @param currency Ссылка на валюту
     * @return Null или {@link Tag}
     */
    default Tag serializeNbt(T currency) {
        return JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, serialize(currency));
    }

    /**
     * Позволяет десериализировать данные через NBT -> JSON -> Object
     * @param id Уникальный идентификатор валюты
     * @param tag NBT data
     * @return Null или {@link T}
     */
    default T deserializeNbt(ResourceLocation id, Tag tag) {
        final JsonElement json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag);
        return deserialize(id, json.getAsJsonObject());
    }
}
