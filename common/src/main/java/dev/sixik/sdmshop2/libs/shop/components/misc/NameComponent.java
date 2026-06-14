package dev.sixik.sdmshop2.libs.shop.components.misc;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.annotation.ComponentConfig;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class NameComponent extends ShopComponent {

    public static final IComponentType<NameComponent> TYPE = new Type();

    @Getter
    @ComponentConfig(translationKey = "shop.component.misc.name.name")
    private String name;

    public NameComponent() {
        this.name = "";
    }

    public NameComponent(String name) {
        this.name = name;
    }


    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    private static class Type implements IComponentType<NameComponent> {

        private static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "name");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public JsonObject serialize(NameComponent component) {
            JsonObject object = new JsonObject();
            object.addProperty("name", component.name);
            return object;
        }

        @Override
        public NameComponent deserialize(JsonObject json) {
            return new NameComponent(json.has("name") ? json.get("name").getAsString() : "");
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, NameComponent component) {
            buf.writeUtf(component.name);
        }

        @Override
        public NameComponent fromNetwork(FriendlyByteBuf buf) {
            return new NameComponent(buf.readUtf());
        }

        @Override
        public NameComponent createDefault() {
            return new NameComponent();
        }
    }
}
