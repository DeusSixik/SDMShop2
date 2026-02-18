package dev.sixik.sdmshop2.libs.shop.components;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import dev.sixik.sdmshop2.libs.shop.base.ShopEntry;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import lombok.Getter;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShopEntriesContainerComponent extends ShopComponent {

    public static final IComponentType<ShopEntriesContainerComponent> TYPE = new Type();

    @Getter
    protected final Map<UUID, ShopEntry> entryMap = new ConcurrentHashMap<>();

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    @Override
    public int priority() {
        return 1000;
    }

    public void addEntry(ShopEntry entry) {
        entryMap.put(entry.getUuid(), entry);
    }

    @Nullable
    public ShopEntry getEntry(UUID entryId) {
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

            final Map<UUID, ShopEntry> map = component.getEntryMap();

            final JsonArray entryArray = json.get("entries").getAsJsonArray();
            for (JsonElement element : entryArray) {
                final JsonObject entryJson = element.getAsJsonObject();

                ShopEntry entry = ShopEntry.createEntry(UUID.fromString(entryJson.get("uuid").getAsString()), true);
                entry.deserialize(entryJson);
                map.put(entry.getUuid(), entry);
            }

            return component;
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShopEntriesContainerComponent component) {
            buf.writeNbt((net.minecraft.nbt.CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, serialize(component)));
        }

        @Override
        public ShopEntriesContainerComponent fromNetwork(FriendlyByteBuf buf) {
            return deserialize(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, buf.readNbt()).getAsJsonObject());
        }
    }
}
