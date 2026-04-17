package dev.sixik.sdmshop2.libs.shop.components.api;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Интерфейс, определяющий метаданные и методы сериализации для типа компонента.
 * Каждый тип компонента должен реализовать этот интерфейс для поддержки сохранения в JSON и передачи по сети.
 *
 * @param <T> Тип компонента, с которым работает данный IComponentType
 */
public interface IComponentType<T extends ShopComponent> {

    /**
     * Возвращает уникальный идентификатор типа компонента.
     *
     * @return ResourceLocation идентификатор
     */
    ResourceLocation getId();

    /**
     * Сериализует компонент в JSON.
     *
     * @param component Экземпляр компонента для сериализации
     * @return JsonObject с данными компонента
     */
    JsonObject serialize(T component);

    /**
     * Десериализует компонент из JSON.
     *
     * @param json JsonObject с данными компонента
     * @return Новый экземпляр компонента
     */
    T deserialize(JsonObject json);

    /**
     * Записывает данные компонента в сетевой буфер.
     *
     * @param buf       Буфер сетевого пакета
     * @param component Экземпляр компонента
     */
    void toNetwork(FriendlyByteBuf buf, T component);

    /**
     * Считывает данные компонента из сетевого буфера и создает новый экземпляр.
     *
     * @param buf Буфер сетевого пакета
     * @return Новый экземпляр компонента
     */
    T fromNetwork(FriendlyByteBuf buf);

    /**
     * Создает экземпляр компонента с настройками по умолчанию.
     *
     * @return Экземпляр компонента по умолчанию
     */
    T createDefault();
}
