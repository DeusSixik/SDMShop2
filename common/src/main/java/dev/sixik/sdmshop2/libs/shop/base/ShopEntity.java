package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ShopEntity {

    private final List<ShopComponent> components = new ArrayList<>();

    public ShopEntity() {
        initializeServerOnlyComponents();
    }

    private void initComponents() {
        final List<ShopComponent> array = components;
        for (int i = 0; i < array.size(); i++) {
            array.get(i).init();
        }
    }

    public final <T extends ShopComponent> T addComponent(T component) {
        component.setRoot(this);
        int i = 0;
        while (i < components.size() && components.get(i).priority() <= component.priority()) {
            i++;
        }
        components.add(i, component);
        return component;
    }

    public final boolean hasComponent(Class<?> type) {
        for (int i = 0; i < components.size(); i++) {
            if (type.isInstance(components.get(i))) {
                return true;
            }
        }
        return false;
    }

    public final List<ShopComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    @SuppressWarnings("unchecked")
    public final <T> List<T> getComponents(Class<T> type) {
        List<T> out = new ArrayList<>();
        for (ShopComponent component : components) {
            if (type.isInstance(component)) {
                out.add((T) component);
            }
        }
        return out;
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

        initializeServerOnlyComponents();
    }

    public JsonElement serialize() {
        return serializeComponents();
    }

    public void deserialize(JsonElement element) {
        deserializeComponents(element);
    }

    public final void serializeComponentsNetwork(FriendlyByteBuf buf) {
        List<ShopComponent> syncList = new ArrayList<>();
        for (ShopComponent component : components) {
            if (component.shouldSync()) {
                syncList.add(component);
            }
        }

        buf.writeVarInt(syncList.size());
        for (ShopComponent component : syncList) {
            ShopComponentRegistry.toNetwork(buf, component);
        }
    }

    public final void deserializeComponentsNetwork(FriendlyByteBuf buf) {
        int count = buf.readVarInt();

        components.clear();
        for (int i = 0; i < count; i++) {
            addComponent(ShopComponentRegistry.fromNetwork(buf));
        }

        initializeClientOnlyComponents();
    }

    public void serializeNetwork(FriendlyByteBuf buf) {
        serializeComponentsNetwork(buf);
    }

    public void deserializeNetwork(FriendlyByteBuf buf) {
        deserializeComponentsNetwork(buf);
    }

    protected final void initializeClientOnlyComponents() {
        customInitializeClientOnlyComponents();
        initComponents();
    }

    protected void customInitializeClientOnlyComponents() { }

    public final void initializeServerOnlyComponents() {
        customInitializeServerOnlyComponents();
        initComponents();
    }

    protected void customInitializeServerOnlyComponents() { }
}
