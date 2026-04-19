package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import lombok.Getter;
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

    private final List<ShopComponent> components = new ArrayList<>();
    private final Map<Class<?>, List<ShopComponent>> componentCache = new IdentityHashMap<>();

    public ShopEntity() { }

    /**
     * Инициализирует все текущие компоненты сущности.
     * Вызывает метод {@link ShopComponent#init()} для каждого компонента.
     */
    private void initComponents() {
        final List<ShopComponent> array = components;
        for (int i = 0; i < array.size(); i++) {
            array.get(i).init();
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
    public final List<ShopComponent> getComponents() {
        return Collections.unmodifiableList(components);
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

        final List<ShopComponent> refArray = components;

        for (int i = 0; i < refArray.size(); i++) {
            compArray.add(ShopComponentRegistry.toJson(refArray.get(i)));
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
            components.add(ShopComponentRegistry.fromJson(compJson.getAsJsonObject()));
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
        final List<ShopComponent> syncList = new ArrayList<>();
        final List<ShopComponent> comps = components;
        for (int i = 0; i < comps.size(); i++) {
            final ShopComponent component = comps.get(i);
            if (component.shouldSync()) {
                syncList.add(component);
            }
        }

        final int size = syncList.size();
        buf.writeVarInt(size);
        for (int i = 0; i < size; i++) {
            ShopComponentRegistry.toNetwork(buf, syncList.get(i));
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
            addComponent(ShopComponentRegistry.fromNetwork(buf));
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
}
