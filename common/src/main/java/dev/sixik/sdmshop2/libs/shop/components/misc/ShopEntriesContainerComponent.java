package dev.sixik.sdmshop2.libs.shop.components.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShopEntriesContainerComponent extends ShopComponent {

    public static final IComponentType<ShopEntriesContainerComponent> TYPE = new Type();

    @Getter
    protected final Map<UUID, ShopOffer> entryMap = new ConcurrentHashMap<>();

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    @Override
    public int priority() {
        return 1000;
    }

    public void addEntry(ShopOffer entry) {
        entryMap.put(entry.getUUID(), entry);
    }

    @Nullable
    public ShopOffer getEntry(UUID entryId) {
        return entryMap.get(entryId);
    }

    private static class Type implements IComponentType<ShopEntriesContainerComponent> {

        public static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "entries_container");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public JsonObject serialize(ShopEntriesContainerComponent component) {
            JsonObject json = new JsonObject();

            JsonArray entryArray = new JsonArray();
            component.entryMap.forEach((id, entry) -> entryArray.add(entry.serialize()));
            json.add("entries", entryArray);

            return json;
        }

        @Override
        public ShopEntriesContainerComponent deserialize(JsonObject json) {
            ShopEntriesContainerComponent component = new ShopEntriesContainerComponent();

            if(!json.has("entries"))
                throw new NullPointerException("Not found 'entries' key!");

            final Map<UUID, ShopOffer> map = component.getEntryMap();

            final JsonArray entryArray = json.get("entries").getAsJsonArray();
            for (JsonElement element : entryArray) {
                final JsonObject entryJson = element.getAsJsonObject();

                ShopOffer entry = ShopOffer.create(UUID.fromString(entryJson.get("uuid").getAsString()), true);
                entry.deserialize(entryJson);
                map.put(entry.getUUID(), entry);
            }

            return component;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShopEntriesContainerComponent component) {
            final Map<UUID, ShopOffer> map = component.getEntryMap();

            buf.writeVarInt(map.size());
            map.forEach((key, value) -> value.serializeNetwork(buf));
        }

        @Override
        public ShopEntriesContainerComponent fromNetwork(FriendlyByteBuf buf) {
            ShopEntriesContainerComponent component = new ShopEntriesContainerComponent();

            int size = buf.readVarInt();

            for (int i = 0; i < size; i++) {
                component.addEntry(ShopOffer.fromNetwork(buf));
            }

            return component;
        }

        @Override
        public ShopEntriesContainerComponent createDefault() {
            return new ShopEntriesContainerComponent();
        }
    }
}
