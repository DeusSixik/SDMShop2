package dev.sixik.sdmshop2.libs.sdmeconomy;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public interface ICurrencyType<T extends IExternalCurrency> {

    T deserialize(ResourceLocation id, JsonObject json);

    JsonObject serialize(T currency);

    default void serializeType(JsonObject object, String type) {
        if(type.contains(":")) {
            object.addProperty("type", type);
        } else {
            object.addProperty("type", "sdm:" + type);
        }
    }
}
