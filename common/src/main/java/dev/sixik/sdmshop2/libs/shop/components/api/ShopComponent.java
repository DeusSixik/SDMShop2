package dev.sixik.sdmshop2.libs.shop.components.api;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.components.api.exceptions.ValidationException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Базовый класс для всех компонентов магазина.
 * Компоненты определяют поведение и данные сущностей магазина ({@link ShopEntity}).
 */
public abstract class ShopComponent {

    /**
     * Константа для обозначения пустого или неопределенного типа компонента.
     */
    public static ResourceLocation EMPTY = ResourceLocation.tryBuild("sdm", "null");

    private ShopEntity root;

    /**
     * Вызывается при инициализации компонента.
     * Используется для настройки начального состояния или связей.
     */
    public void init() { }

    /**
     * Определяет приоритет инициализации и обработки компонента.
     * Компоненты с меньшим значением приоритета обрабатываются раньше.
     *
     * @return Приоритет (по умолчанию 0)
     */
    public int priority() {
        return 0;
    }

    /**
     * Возвращает тип компонента, который содержит логику сериализации.
     *
     * @return Объект типа компонента
     */
    public abstract IComponentType<?> getType();

    /**
     * Возвращает корневую сущность, которой принадлежит данный компонент.
     *
     * @return Корневая сущность
     */
    public final ShopEntity getRoot() {
        return root;
    }

    /**
     * Возвращает корневую сущность, приведенную к указанному типу.
     *
     * @param <T> Тип сущности
     * @return Корневая сущность
     */
    public final <T extends ShopEntity> T getRoots() {
        return (T) root;
    }

    /**
     * Устанавливает корневую сущность для компонента.
     * Может быть установлена только один раз.
     *
     * @param entity Корневая сущность
     */
    public final void setRoot(ShopEntity entity) {
        if(root != null) return;
        this.root = entity;
    }

    /**
     * Определяет, должен ли компонент синхронизироваться с клиентом по сети.
     *
     * @return true, если компонент должен быть отправлен на клиент, иначе false
     */
    public boolean shouldSync() {
        return true;
    }

    /**
     * Проверяет валидность данных компонента.
     * @deprecated Используйте более современные механизмы валидации, если они предусмотрены.
     * @throws ValidationException если данные некорректны
     */
    @Deprecated
    public void validate() throws ValidationException {}

    /**
     * Проверяет, является ли переданный идентификатор идентификатором пустого компонента.
     *
     * @param id Идентификатор типа компонента
     * @return true, если это EMPTY, иначе false
     */
    public static boolean isEmpty(ResourceLocation id) {
        return EMPTY.equals(id);
    }

    public void additionalSerialize(JsonObject json) { }

    public void additionalDeserialize(JsonObject json) { }

    public void additionalToNetwork(FriendlyByteBuf buf) { }

    public void additionalFromNetwork(FriendlyByteBuf buf) { }
}
