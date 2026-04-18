package dev.sixik.sdmshop2.libs.shop.components.misc;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class CatalogComponent extends ShopComponent {

    public static final IComponentType<CatalogComponent> TYPE = new Type();

    protected static final String NULL = "none";

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private UUID uuid;

    public CatalogComponent() {
        this(NULL);
    }

    public CatalogComponent(String id) {
        this(id, UUID.randomUUID());
    }

    public CatalogComponent(String id, UUID uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    private static class Type implements IComponentType<CatalogComponent> {

        public static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "catalog");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public CatalogComponent deserialize(JsonObject json) {
            return new CatalogComponent(
                    (json.has("catalog_id") ?
                            json.get("catalog_id").getAsString() :
                            NULL),
                    json.has("uuid") ? UUID.fromString(json.get("uuid").getAsString()) : UUID.randomUUID()
            );
        }

        @Override
        public JsonObject serialize(CatalogComponent component) {
            JsonObject object = new JsonObject();
            object.addProperty("catalog_id", component.id);
            object.addProperty("uuid", component.uuid.toString());
            return object;
        }

        @Override
        public CatalogComponent fromNetwork(FriendlyByteBuf buf) {
            return new CatalogComponent(buf.readUtf());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CatalogComponent component) {
            buf.writeUtf(component.getId());
        }

        @Override
        public CatalogComponent createDefault() {
            return new CatalogComponent();
        }
    }
}
