package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import lombok.Getter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ShopEntity {

    @Getter
    private final List<ShopComponent> components = new ArrayList<>();

    public ShopEntity() {
        initializeComponents();
    }

    private void initComponents() {
        final List<ShopComponent> array = components;
        for (int i = 0; i < array.size(); i++) {
            array.get(i).init();
        }
    }

    public final ShopEntity addComponent(ShopComponent component) {
        component.setRoot(this);
        int i = 0;
        while (i < components.size() && components.get(i).priority() <= component.priority()) {
            i++;
        }
        components.add(i, component);
        return this;
    }

    public final <T> boolean hasComponent(Class<T> type) {
        for (ShopComponent component : components) {
            if (type.isInstance(component)) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    public final <T> List<T> getComponents(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (ShopComponent component : components) {
            if (type.isInstance(component)) {
                result.add((T) component);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public final <T> Optional<T> getComponent(Class<T> type) {
        for (ShopComponent component : components) {
            if (type.isInstance(component)) {
                return Optional.of((T) component);
            }
        }
        return Optional.empty();
    }

    public final JsonArray serializeComponents() {
        JsonArray compArray = new JsonArray();

        final List<ShopComponent> refArray = components;

        for (int i = 0; i < refArray.size(); i++) {
            compArray.add(ShopComponentRegistry.toJson(refArray.get(i)));
        }

        return compArray;
    }

    public final void deserializeComponents(JsonElement element) {
        deserializeComponents(element.getAsJsonArray());
    }

    public final void deserializeComponents(JsonArray array) {
        components.clear();

        for (JsonElement compJson : array) {
            components.add(ShopComponentRegistry.fromJson(compJson.getAsJsonObject()));
        }

        initComponents();
    }

    public JsonElement serialize() {
        return serializeComponents();
    }

    public void deserialize(JsonElement element) {
        deserializeComponents(element);
    }

    public final void serializeComponentsNetwork(FriendlyByteBuf buf) {
        buf.writeNbt((net.minecraft.nbt.CompoundTag) JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, serializeComponents()));
    }

    public final void deserializeComponentsNetwork(FriendlyByteBuf buf) {
        deserializeComponents(NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, buf.readAnySizeNbt()));
    }

    public void serializeNetwork(FriendlyByteBuf buf) {
        serializeComponentsNetwork(buf);
    }

    public void deserializeNetwork(FriendlyByteBuf buf) {
        deserializeComponentsNetwork(buf);
    }

    public final void initializeComponents() {
        customInitializeComponents();
        initComponents();
    }

    protected void customInitializeComponents() {}
}
