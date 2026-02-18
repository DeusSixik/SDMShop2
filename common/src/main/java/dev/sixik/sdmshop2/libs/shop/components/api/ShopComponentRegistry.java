package dev.sixik.sdmshop2.libs.shop.components.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ShopComponentRegistry {

    private static final Map<ResourceLocation, IComponentType<?>> TYPES = new HashMap<>();

    public static void register(IComponentType<?> type) {
        TYPES.put(type.getId(), type);
    }

    public static JsonObject toJson(ShopComponent component) {
        IComponentType type = component.getType();

        final JsonObject json = type.serialize(component);
        json.addProperty("type", type.getId().toString());
        return json;
    }

    public static ShopComponent fromJson(JsonObject json) {
        if (!json.has("type")) throw new JsonSyntaxException("Component missing 'type'");
        ResourceLocation id = new ResourceLocation(json.get("type").getAsString());

        IComponentType<?> type = TYPES.get(id);
        if (type == null) throw new JsonSyntaxException("Unknown component type: " + id);

        return type.deserialize(json);
    }

    public static void toNetwork(FriendlyByteBuf buf, ShopComponent component) {
        IComponentType type = component.getType();
        buf.writeResourceLocation(type.getId());
        type.toNetwork(buf, component);
    }

    public static ShopComponent fromNetwork(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        IComponentType<?> type = TYPES.get(id);

        return type.fromNetwork(buf);
    }
}
