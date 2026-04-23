package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.platform.ServerOperation;
import dev.sixik.sdmshop2.libs.platform.ThreadingOperationTimeSave;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
import dev.sixik.sdmshop2.libs.shop.config.ShopConfig;
import dev.sixik.sdmshop2.libs.shop.events.ShopServerEvents;
import dev.sixik.sdmshop2.libs.shop.scripting.events.ShopScriptEvents;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Глобальный менеджер всех магазинов в системе.
 * Отвечает за хранение, загрузку, сохранение и удаление экземпляров {@link ShopInstance}.
 */
public final class ShopTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopTable.class);

    /**
     * Глобальный экземпляр ShopTable.
     */
    public static ShopTable Instance;

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final ExecutorService ioExecutor;
    private volatile Object2ObjectMap<ResourceLocation, ShopInstance> shops = new Object2ObjectOpenHashMap<>();
    private final Object writeLock = new Object();

    /**
     * Путь к директории магазинов в папке сохранения мира.
     */
    @Getter
    private final Path shopDirWorld;

    /**
     * Путь к корневой директории настроек магазина.
     */
    @Getter
    private final Path shopDirConfig;

    /**
     * Путь к директории, где хранятся JSON-файлы магазинов.
     */
    @Getter
    private final Path shopsDir;

    /**
     * Текущий экземпляр Minecraft сервера.
     */
    @Getter
    private final MinecraftServer server;

    @Getter
    private volatile boolean reloading = false;

    public ShopTable(MinecraftServer server) {
        this.ioExecutor = Executors.newSingleThreadExecutor();
        this.server = server;
        this.shopDirWorld = SDMEconomyPlatform.resolveSdmDir(server.getWorldPath(LevelResource.ROOT), "shop");
        this.shopDirConfig = SDMEconomyPlatform.resolveSdmDir(Platform.getConfigFolder(), "shop");
        this.shopsDir = SDMEconomyPlatform.resolveSdmDir(Platform.getConfigFolder(), "shop/shops");

        reload();
    }

    /**
     * Создает и регистрирует новый магазин.
     *
     * @param shopId Уникальный ID нового магазина
     */
    public void addShop(ResourceLocation shopId) {
        addShop(ShopInstance.createManager(shopId, true));
    }

    /**
     * Добавляет существующий экземпляр магазина в таблицу.
     *
     * @param instance Экземпляр магазина
     */
    public void addShop(ShopInstance instance) {
        synchronized (writeLock) {
            Object2ObjectMap<ResourceLocation, ShopInstance> newMap = new Object2ObjectOpenHashMap<>(shops);
            newMap.put(instance.getId(), instance);

            shops = newMap;
        }
    }

    /**
     * Возвращает экземпляр магазина по его ID.
     *
     * @param id ID искомого магазина
     * @return Экземпляр магазина или null, если не найден
     */
    @Nullable
    public ShopInstance getShop(ResourceLocation id) {
        return shops.get(id);
    }

    /**
     * Возвращает коллекцию всех зарегистрированных магазинов.
     *
     * @return Все магазины
     */
    public Collection<ShopInstance> getAllShops() {
        return shops.values();
    }

    /**
     * Возвращает коллекцию IDs всех зарегистрированных магазинов.
     *
     * @return Коллекция строковых ID магазинов
     */
    public List<ResourceLocation> getShopsId() {
        return shops.keySet().stream().toList();
    }

    /**
     * Удаляет магазин и его файл.
     *
     * @param instance Экземпляр магазина для удаления
     */
    public void deleteShop(ShopInstance instance) {
        deleteShop(instance.getId());
    }

    /**
     * Удаляет магазин по его ID и удаляет соответствующий файл JSON.
     *
     * @param id ID магазина для удаления
     */
    public void deleteShop(ResourceLocation id) {
        synchronized (writeLock) {
            Object2ObjectMap<ResourceLocation, ShopInstance> newMap = new Object2ObjectOpenHashMap<>(shops);
            ShopInstance removed = newMap.remove(id);

            shops = newMap;

            if (removed != null) {
                ioExecutor.submit(() -> {
                    try {
                        Files.deleteIfExists(shopsDir.resolve(id.getPath() + ".json"));
                    } catch (IOException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                });
            }
        }
    }

    /**
     * Синхронно сохраняет все магазины в файлы.
     */
    public void saveAll() {
        for (ShopInstance value : shops.values()) {
            save(value);
        }
    }

    /**
     * Асинхронно сохраняет все магазины в файлы.
     */
    public void saveAllAsync() {
        for (ShopInstance value : shops.values()) {
            saveAsync(value);
        }
    }

    /**
     * Сохраняет указанный магазин в файл.
     *
     * @param instance Экземпляр магазина
     */
    public void save(ShopInstance instance) {
        saveShopToFile(instance);
    }

    /**
     * Асинхронно сохраняет указанный магазин в файл.
     *
     * @param instance Экземпляр магазина
     */
    public void saveAsync(ShopInstance instance) {
        ioExecutor.submit(() -> saveShopToFile(instance));
    }

    /**
     * Перезагружает все данные магазинов из папки {@link #getShopsDir()}.
     * Все текущие данные будут перезаписанны через подмену ссылок
     */
    public void reload() {
        if(reloading) return;

        reloading = true;
        try {
            LOGGER.info("Start reloading shops data!");

            Object2ObjectMap<ResourceLocation, ShopInstance> loadedShops = new Object2ObjectOpenHashMap<>();

            if (!Files.exists(shopsDir)) {
                Files.createDirectories(shopsDir);
            }

            try (Stream<Path> files = Files.walk(shopsDir)) {
                files.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(path -> loadShopFromFile(path, loadedShops));
            }

            synchronized (writeLock) {
                shops = loadedShops;
            }

            ShopScriptEvents.SCRIPT_SHOP_LOAD_EVENT.invoker().invoke(server, this);
            ShopServerEvents.SHOP_LOAD_EVENT.invoker().invoke(server, this);

            LOGGER.info("Loaded {} shops.", shops.size());
        } catch (Exception e) {
            LOGGER.error("Failed to load shops", e);
        } finally {
            reloading = false;
        }
    }

    private void loadShopFromFile(Path path, Object2ObjectMap<ResourceLocation, ShopInstance> mapToFill) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            final ShopInstance shop = ShopInstance.fromJson(json);
            mapToFill.put(shop.getId(), shop);
        } catch (Exception e) {
            LOGGER.error("Error loading shop: {}", path, e);
        }
    }

    private void saveShopToFile(ShopInstance shop) {
        if(!shop.shouldSave()) return;

        try {
            Path path = shopsDir.resolve(shop.getId().getPath() + ".json");

            if (shop.getId().getPath().contains("/")) {
                Files.createDirectories(path.getParent());
            }

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(shop.serialize(), writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save shop {}", shop.getId(), e);
        }
    }

    public static class Manager implements ServerOperation, ThreadingOperationTimeSave {

        @Override
        public void onReload() {
            if(ShopTable.Instance != null) {
                ShopTable.Instance.reload();
            }
        }

        @Override
        public void onServerStart(MinecraftServer server) {
            ShopTable.Instance = new ShopTable(server);
        }

        @Override
        public void onServerStop(MinecraftServer server) {
            ShopTable.Instance.saveAll();
        }

        @Override
        public void onDataStartSave() {
            if(ShopTable.Instance == null) return;
            ShopTable.Instance.saveAll();
        }

        @Override
        public int getDataSaveTimeSeconds() {
            final ShopConfig config = SDMShop2.getConfig();
            return config.autoSaveShopData ? config.saveShopDataIntervalSeconds : -1;
        }
    }
}
