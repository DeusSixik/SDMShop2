package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.CategoryComponent;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public class ShopEntry extends ShopEntity {

    public static ShopEntry createEntry(UUID uuid, boolean initializeComponents) {
        ShopEntry entry = new ShopEntry(uuid);

        if(initializeComponents)
            entry.initializeServerOnlyComponents();
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
    public void serializeNetwork(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        super.serializeNetwork(buf);
    }

    @Override
    protected void customInitializeServerOnlyComponents() {
        addComponent(new CategoryComponent("none"));
    }

    public static ShopEntry fromNetwork(FriendlyByteBuf buf) {
        final ShopEntry entry = new ShopEntry(buf.readUUID());
        entry.deserializeNetwork(buf);
        return entry;
    }
}
