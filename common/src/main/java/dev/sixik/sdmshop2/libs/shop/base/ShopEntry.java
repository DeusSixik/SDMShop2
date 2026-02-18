package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.CategoryComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

public class ShopEntry extends ShopEntity {

    public static ShopEntry createEntry(UUID uuid, boolean initializeComponents) {
        ShopEntry entry = new ShopEntry(uuid);

        if(initializeComponents)
            entry.initializeComponents();
        return entry;
    }

    @Getter
    private final UUID uuid;

    protected ShopEntry(UUID uuid) {
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
    protected void customInitializeComponents() {
        addComponent(new CategoryComponent("none"));
    }
}
