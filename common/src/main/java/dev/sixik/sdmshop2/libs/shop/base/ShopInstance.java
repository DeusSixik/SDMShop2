package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.ShopCategoriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.ShopOffersContainerComponent;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Представляет экземпляр магазина.
 * Является наследником {@link ShopEntity} и содержит компоненты для управления записями и категориями.
 */
public class ShopInstance extends ShopEntity {

    public static final ResourceLocation NULL_MANAGER = ResourceLocation.tryBuild("sdm", "null");

    /**
     * Создает новый экземпляр магазина.
     *
     * @param shopId               Уникальный ID магазина
     * @param initializeComponents Нужно ли сразу инициализировать компоненты сервера
     * @return Новый экземпляр ShopInstance
     */
    public static ShopInstance createManager(ResourceLocation shopId, boolean initializeComponents) {
        ShopInstance manager = new ShopInstance(shopId);

        if(initializeComponents)
            manager.initializeServerOnlyComponents();

        return manager;
    }

    /**
     * Уникальный идентификатор магазина.
     */
    @Getter
    protected final ResourceLocation id;

    @Setter
    protected boolean shouldSave = true;

    @Getter
    private byte[] networkCache = null;

    private boolean dirty = false;

    @Getter
    @Setter
    private Runnable onUpdate = () -> {};

    private ShopInstance(ResourceLocation shopId) {
        this.id = shopId;
    }

    @Override
    protected void customInitializeServerOnlyComponents() {
        if(!hasComponent(ShopOffersContainerComponent.class))
            addComponent(new ShopOffersContainerComponent());

        if(!hasComponent(ShopCategoriesContainerComponent.class))
            addComponent(new ShopCategoriesContainerComponent());
    }

    @Override
    protected void customInitializeClientOnlyComponents() {
        customInitializeServerOnlyComponents();
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
        } else initializeServerOnlyComponents();
    }

    @Override
    public void serializeNetwork(FriendlyByteBuf buf) {
        if(this.networkCache == null || this.dirty) {
            updateNetworkCache();
        }

        buf.writeResourceLocation(id);
        buf.writeBytes(this.networkCache);
    }

    /**
     * Создает экземпляр магазина из данных, полученных по сети.
     *
     * @param buf Буфер сетевого пакета
     * @return Восстановленный экземпляр ShopInstance
     */
    public static ShopInstance fromNetwork(FriendlyByteBuf buf) {
        final ResourceLocation id = buf.readResourceLocation();

        final ShopInstance shopInstance = new ShopInstance(id);
        shopInstance.deserializeComponentsNetwork(buf);
        return shopInstance;
    }

    /**
     * Создает экземпляр магазина из данных JSON.
     *
     * @param element Элемент JSON
     * @return Восстановленный экземпляр ShopInstance
     * @throws JsonParseException если поле 'id' отсутствует
     */
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

    /**
     * Возвращает компонент-контейнер для записей (офферов) магазина.
     *
     * @return Контейнер записей
     * @throws IllegalStateException если компонент отсутствует
     */
    public ShopOffersContainerComponent getEntries() {
        return getComponent(ShopOffersContainerComponent.class)
                .orElseThrow(() -> new IllegalStateException("Shop " + id + " corrupted: missing EntriesComponent"));
    }

    /**
     * Возвращает компонент-контейнер для категорий магазина.
     *
     * @return Контейнер категорий
     * @throws IllegalStateException если компонент отсутствует
     */
    public ShopCategoriesContainerComponent getCategories() {
        return getComponent(ShopCategoriesContainerComponent.class)
                .orElseThrow(() -> new IllegalStateException("Shop " + id + " corrupted: missing CategoriesComponent"));
    }

    /**
     * Проверяет, является ли этот экземпляр магазина "пустым".
     *
     * @return true, если это NULL_MANAGER, иначе false
     */
    public final boolean isNull() {
        return id.equals(NULL_MANAGER);
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    @Override
    protected void onUpdate() {
        setDirty();
    }

    public void setDirty() {
        dirty = true;
        onUpdate.run();
    }

    private void updateNetworkCache() {
        final FriendlyByteBuf friendlyTempBuf = new FriendlyByteBuf(Unpooled.buffer());

        try {
            super.serializeNetwork(friendlyTempBuf);

            final int readableBytes = friendlyTempBuf.readableBytes();
            this.networkCache = new byte[readableBytes];
            friendlyTempBuf.readBytes(this.networkCache);

            this.dirty = false;
        } finally {
            friendlyTempBuf.release();
        }
    }
}
