package dev.sixik.sdmshop2.libs.shop.components.misc;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class CategoryComponent extends ShopComponent {

    public static final IComponentType<CategoryComponent> TYPE = new Type();

    protected static final String NULL = "none";

    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private UUID uuid;

    public CategoryComponent() {
        this(NULL);
    }

    public CategoryComponent(String id) {
        this(id, UUID.randomUUID());
    }

    public CategoryComponent(String id, UUID uuid) {
        this.id = id;
        this.uuid = uuid;
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    private static class Type implements IComponentType<CategoryComponent> {

        public static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "category");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public CategoryComponent deserialize(JsonObject json) {
            return new CategoryComponent(
                    (json.has("category_id") ?
                            json.get("category_id").getAsString() :
                            NULL),
                    UUID.fromString(json.get("uuid").getAsString())
            );
        }

        @Override
        public JsonObject serialize(CategoryComponent component) {
            JsonObject object = new JsonObject();
            object.addProperty("category_id", component.id);
            object.addProperty("uuid", component.uuid.toString());
            return object;
        }

        @Override
        public CategoryComponent fromNetwork(FriendlyByteBuf buf) {
            return new CategoryComponent(buf.readUtf());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CategoryComponent component) {
            buf.writeUtf(component.getId());
        }

        @Override
        public CategoryComponent createDefault() {
            return new CategoryComponent();
        }
    }
}
