package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.*;

public class ShopEntity {

    private boolean initialized = false;

    private final List<ShopComponent> components = new ArrayList<>();
    private final Map<Class<?>, List<ShopComponent>> componentCache = new IdentityHashMap<>();

    public ShopEntity() { }

    private void initComponents() {
        final List<ShopComponent> array = components;
        for (int i = 0; i < array.size(); i++) {
            array.get(i).init();
        }
        initialized = true;
    }

    public final <T extends ShopComponent> T addComponent(T component) {
        component.setRoot(this);
        int i = 0;
        while (i < components.size() && components.get(i).priority() <= component.priority()) {
            i++;
        }
        components.add(i, component);

        componentCache.clear();

        if(initialized)
            component.init();

        return component;
    }

    public final boolean hasComponent(Class<?> type) {
       return !getComponents(type).isEmpty();
    }

    public final List<ShopComponent> getComponents() {
        return Collections.unmodifiableList(components);
    }

    @SuppressWarnings("unchecked")
    public final <T> List<T> getComponents(Class<T> type) {
        /*
            Ленивое кэширование. Фильтруем только при первом запросе конкретного типа.
         */
        return (List<T>) componentCache.computeIfAbsent(type, k -> {
            List<ShopComponent> filtered = new ArrayList<>();
            for (int i = 0; i < components.size(); i++) {
                ShopComponent c = components.get(i);
                if (k.isInstance(c)) {
                    filtered.add(c);
                }
            }

            /*
                Используем emptyList() для экономии памяти, если компонентов нет
             */
            return filtered.isEmpty() ? Collections.emptyList() : Collections.unmodifiableList(filtered);
        });
    }

    public final <T> Optional<T> getComponent(Class<T> type) {
        List<T> list = getComponents(type);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
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
        componentCache.clear();

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
        componentCache.clear();
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
        initialized = false;
        customInitializeClientOnlyComponents();
        initComponents();
    }

    protected void customInitializeClientOnlyComponents() { }

    public final void initializeServerOnlyComponents() {
        initialized = false;
        customInitializeServerOnlyComponents();
        initComponents();
    }

    protected void customInitializeServerOnlyComponents() { }
}
