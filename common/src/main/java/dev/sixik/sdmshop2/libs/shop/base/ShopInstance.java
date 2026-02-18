package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.sixik.sdmshop2.libs.shop.components.ShopEntriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.ShopCategoriesContainerComponent;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

public class ShopInstance extends ShopEntity {

    public static final ResourceLocation NULL_MANAGER = ResourceLocation.tryBuild("sdm", "null");

    public static ShopInstance createManager(ResourceLocation shopId, boolean initializeComponents) {
        ShopInstance manager = new ShopInstance(shopId);

        if(initializeComponents)
            manager.initializeComponents();

        return manager;
    }

    @Getter
    protected final ResourceLocation id;

    private ShopInstance(ResourceLocation shopId) {
        this.id = shopId;
    }

    @Override
    protected void customInitializeComponents() {
        addComponent(new ShopEntriesContainerComponent());
        addComponent(new ShopCategoriesContainerComponent());
    }

    @Override
    public JsonElement serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("id", id.toString());
        json.add("components", serializeComponents());
        return json;
    }

    @Override
    public void deserialize(JsonElement element) {
        if (!element.isJsonObject()) return;
        JsonObject json = element.getAsJsonObject();

        if (json.has("components")) {
            deserializeComponents(json.get("components").getAsJsonArray());
        } else initializeComponents();
    }

    public static ShopInstance fromJson(JsonElement element) {
        JsonObject json = element.getAsJsonObject();

        if (!json.has("id")) {
            throw new JsonParseException("Shop missing 'id' field");
        }

        ResourceLocation id = new ResourceLocation(json.get("id").getAsString());
        ShopInstance shop = new ShopInstance(id);
        shop.deserialize(json);
        return shop;
    }

    public ShopEntriesContainerComponent getEntries() {
        return getComponent(ShopEntriesContainerComponent.class)
                .orElseThrow(() -> new IllegalStateException("Shop " + id + " corrupted: missing EntriesComponent"));
    }

    public ShopCategoriesContainerComponent getCategories() {
        return getComponent(ShopCategoriesContainerComponent.class)
                .orElseThrow(() -> new IllegalStateException("Shop " + id + " corrupted: missing CategoriesComponent"));
    }

    public final boolean isNull() {
        return id.equals(NULL_MANAGER);
    }
}
