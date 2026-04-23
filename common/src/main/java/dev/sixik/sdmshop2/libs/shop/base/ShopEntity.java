package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;

import java.util.*;

/**
 * Базовая сущность в архитектуре Entity-Component System (ECS) магазина.
 * Выступает в роли контейнера для компонентов ({@link ShopComponent}), которые
 * определяют логику и свойства объекта. Управляет инициализацией, хранением,
 * кэшированием и сериализацией компонентов.
 */
public class ShopEntity {

    private boolean initialized = false;

    private final ObjectArrayList<ShopComponent> components = new ObjectArrayList<>();
    private final Map<Class<?>, ObjectList<ShopComponent>> componentCache = new Reference2ObjectOpenHashMap<>();

    public ShopEntity() { }

    /**
     * Инициализирует все текущие компоненты сущности.
     * Вызывает метод {@link ShopComponent#init()} для каждого компонента.
     */
    private void initComponents() {
        final Object[] array = components.elements();
        final int size = components.size();
        for (int i = 0; i < size; i++) {
            ((ShopComponent)array[i]).init();
        }
        initialized = true;
    }

    /**
     * Добавляет новый компонент к сущности с учетом его приоритета выполнения.
     * Если сущность уже инициализирована, автоматически вызывает метод init() у компонента.
     * Сбрасывает кэш компонентов.
     *
     * @param component Добавляемый компонент
     * @param <T>       Тип компонента
     * @return Добавленный компонент для цепочечных вызовов
     */
    public final <T extends ShopComponent> T addComponent(T component) {
        component.setRoot(this);

        final Object[] primitiveArray = components.elements();
        final int size = components.size();

        int i = 0;
        while (i < size && ((ShopComponent) primitiveArray[i]).priority() <= component.priority()) {
            i++;
        }

        components.add(i, component);
        componentCache.clear();

        if(initialized)
            component.init();

        onAddComponent(component);
        return component;
    }

    /**
     * Проверяет наличие компонента указанного типа у сущности.
     *
     * @param type Класс искомого типа компонента
     * @return true, если компонент найден, иначе false
     */
    public final boolean hasComponent(Class<?> type) {
       return !getComponents(type).isEmpty();
    }

    /**
     * Возвращает неизменяемый список всех компонентов сущности.
     *
     * @return Список всех компонентов
     */
    public final ObjectList<ShopComponent> getComponents() {
        return ObjectLists.unmodifiable(components);
    }

    /**
     * Возвращает неизменяемый список всех компонентов указанного типа.
     * Использует ленивое кэширование: фильтрация происходит только при первом запросе
     * данного класса, после чего результат сохраняется в {@code componentCache}.
     *
     * @param type Класс искомого типа компонентов
     * @param <T>  Тип компонентов
     * @return Список компонентов, соответствующих указанному типу (или пустой список)
     */
    @SuppressWarnings("unchecked")
    public final <T> ObjectList<T> getComponents(Class<T> type) {
        /*
            Ленивое кэширование. Фильтруем только при первом запросе конкретного типа.
         */
        return (ObjectList<T>) componentCache.computeIfAbsent(type, k -> {
            final ObjectArrayList<ShopComponent> filtered = new ObjectArrayList<>();
            final Object[] primitiveComponents = components.elements();
            for (int i = 0; i < components.size(); i++) {
                ShopComponent c = (ShopComponent) primitiveComponents[i];
                if (k.isInstance(c)) {
                    filtered.add(c);
                }
            }

            /*
                Используем emptyList() для экономии памяти, если компонентов нет
             */
            return filtered.isEmpty() ? ObjectLists.emptyList() : ObjectLists.unmodifiable(filtered);
        });
    }

    /**
     * Возвращает первый найденный компонент указанного типа, обернутый в {@link Optional}.
     *
     * @param type Класс искомого типа компонента
     * @param <T>  Тип компонента
     * @return Optional с компонентом, если он найден, иначе Optional.empty()
     */
    public final <T> Optional<T> getComponent(Class<T> type) {
        List<T> list = getComponents(type);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Сериализует все компоненты сущности в {@link JsonArray}.
     * Использует {@link ShopComponentRegistry#toJson(ShopComponent)} для каждого компонента.
     *
     * @return Массив JSON с данными компонентов
     */
    public final JsonArray serializeComponents() {
        JsonArray compArray = new JsonArray();

        final Object[] refArray = components.elements();
        final int size = components.size();

        for (int i = 0; i < size; i++) {
            compArray.add(ShopComponentRegistry.toJson((ShopComponent) refArray[i]));
        }

        return compArray;
    }

    /**
     * Десериализует компоненты из {@link JsonElement}.
     *
     * @param element Элемент JSON (ожидается JsonArray)
     */
    public final void deserializeComponents(JsonElement element) {
        deserializeComponents(element.getAsJsonArray());
    }

    /**
     * Десериализует компоненты из {@link JsonArray}.
     * Очищает текущие компоненты и кэш, затем загружает новые.
     * После десериализации вызывает {@link #initializeServerOnlyComponents()}.
     *
     * @param array Массив JSON с данными компонентов
     */
    public final void deserializeComponents(JsonArray array) {
        components.clear();
        componentCache.clear();

        for (JsonElement compJson : array) {
            final ShopComponent component = ShopComponentRegistry.fromJson(compJson.getAsJsonObject());
            component.setRoot(this);
            components.add(component);
        }

        initializeServerOnlyComponents();
    }

    /**
     * Сериализует сущность. По умолчанию сериализует только компоненты.
     *
     * @return Элемент JSON с данными сущности
     */
    public JsonElement serialize() {
        return serializeComponents();
    }

    /**
     * Десериализует сущность. По умолчанию десериализует только компоненты.
     *
     * @param element Элемент JSON с данными сущности
     */
    public void deserialize(JsonElement element) {
        deserializeComponents(element);
    }

    /**
     * Сериализует компоненты для передачи по сети.
     * Отправляет только те компоненты, для которых {@link ShopComponent#shouldSync()} возвращает true.
     *
     * @param buf Буфер сетевого пакета
     */
    public final void serializeComponentsNetwork(FriendlyByteBuf buf) {
        final ObjectArrayList<ShopComponent> syncList = new ObjectArrayList<>();
        final Object[] comps = components.elements();
        final int compSize = components.size();
        for (int i = 0; i < compSize; i++) {
            final ShopComponent component = (ShopComponent) comps[i];
            if (component.shouldSync()) {
                syncList.add(component);
            }
        }

        final Object[] primitiveSyncList = syncList.elements();
        final int syncSize = syncList.size();
        buf.writeVarInt(syncSize);
        for (int i = 0; i < syncSize; i++) {
            ShopComponentRegistry.toNetwork(buf, (ShopComponent) primitiveSyncList[i]);
        }
    }

    /**
     * Десериализует компоненты из сетевого буфера.
     * Очищает текущие компоненты и кэш, затем считывает новые.
     * После десериализации вызывает {@link #initializeClientOnlyComponents()}.
     *
     * @param buf Буфер сетевого пакета
     */
    public final void deserializeComponentsNetwork(FriendlyByteBuf buf) {
        int count = buf.readVarInt();

        components.clear();
        componentCache.clear();
        for (int i = 0; i < count; i++) {
            final ShopComponent component = ShopComponentRegistry.fromNetwork(buf);
            component.setRoot(this);
            addComponent(component);
        }

        initializeClientOnlyComponents();
    }

    /**
     * Сериализует сущность для передачи по сети.
     * По умолчанию сериализует только компоненты.
     *
     * @param buf Буфер сетевого пакета
     */
    public void serializeNetwork(FriendlyByteBuf buf) {
        serializeComponentsNetwork(buf);
    }

    /**
     * Десериализует сущность из сетевого буфера.
     * По умолчанию десериализует только компоненты.
     *
     * @param buf Буфер сетевого пакета
     */
    public void deserializeNetwork(FriendlyByteBuf buf) {
        deserializeComponentsNetwork(buf);
    }

    /**
     * Выполняет инициализацию компонентов, специфичных для клиента.
     * Сбрасывает флаг инициализации, вызывает {@link #customInitializeClientOnlyComponents()} и {@link #initComponents()}.
     */
    protected final void initializeClientOnlyComponents() {
        initialized = false;
        customInitializeClientOnlyComponents();
        initComponents();
    }

    /**
     * Метод для переопределения в наследниках.
     * Используется для добавления компонентов, которые должны быть только на стороне клиента.
     */
    protected void customInitializeClientOnlyComponents() { }

    /**
     * Выполняет инициализацию компонентов, специфичных для сервера.
     * Сбрасывает флаг инициализации, вызывает {@link #customInitializeServerOnlyComponents()} и {@link #initComponents()}.
     */
    public final void initializeServerOnlyComponents() {
        initialized = false;
        customInitializeServerOnlyComponents();
        initComponents();
    }

    /**
     * Метод для переопределения в наследниках.
     * Используется для добавления компонентов, которые должны быть только на стороне сервера.
     */
    protected void customInitializeServerOnlyComponents() { }

    protected void onAddComponent(ShopComponent component) { }

}
