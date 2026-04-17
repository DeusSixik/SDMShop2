package dev.sixik.sdmshop2.libs.shop.base.limiter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.platform.ServerOperation;
import dev.sixik.sdmshop2.libs.platform.ThreadingOperationTimeSave;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
import dev.sixik.sdmshop2.libs.shop.config.ShopConfig;
import dev.sixik.sdmshop2.utils.exceptions.NotInitializedException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Серверная реализация таблицы лимитов магазина.
 * Хранит полную базу данных: глобальные лимиты всех товаров и персональные лимиты всех игроков.
 * Обеспечивает потокобезопасное асинхронное сохранение данных в JSON файл.
 */
public final class ShopLimiterTableServer implements ShopLimiterTable {

    private static ShopLimiterTableServer Instance;

    public static ShopLimiterTableServer getInstance() {
        if(Instance == null)
            throw new NotInitializedException("ShopLimiterTableServer is not initialized yet.");

        return Instance;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopLimiterTableServer.class);
    private final ThreadLocal<Gson> GSON_LOCAL = ThreadLocal.withInitial(() -> new GsonBuilder().setPrettyPrinting().create());
    private final ExecutorService ioExecutor;
    private final Path saveFile;

    private final Map<UUID, ShopLimiterEntityData> entitiesData = new ConcurrentHashMap<>();
    private final Map<UUID, ShopLimiterPlayerData> playersData = new HashMap<>();

    public ShopLimiterTableServer(Path shopDirWorld) {
        this(shopDirWorld, false);
    }

    public ShopLimiterTableServer(MinecraftServer server) {
        this(server, false);
    }

    public ShopLimiterTableServer(MinecraftServer server, boolean isInstance) {
        this(SDMEconomyPlatform.resolveSdmDir(server.getWorldPath(LevelResource.ROOT), "shop"), isInstance);
    }

    public ShopLimiterTableServer(Path shopDirWorld, boolean isInstance) {
        this.ioExecutor = Executors.newSingleThreadExecutor();
        this.saveFile = shopDirWorld.resolve("limiter_data.json");

        load();

        if(isInstance)
            Instance = this;
    }

    public ShopLimiterEntityData getEntityData(UUID entityId) {
        return entitiesData.computeIfAbsent(entityId, ShopLimiterEntityData::new);
    }

    public ShopLimiterPlayerData getPlayerData(Player player) {
        return playersData.computeIfAbsent(player.getGameProfile().getId(), ShopLimiterPlayerData::new);
    }

    public ShopLimiterPlayerData getPlayerData(UUID playerId) {
        return playersData.computeIfAbsent(playerId, ShopLimiterPlayerData::new);
    }

    public void clear() {
        entitiesData.clear();
        playersData.clear();
    }

    @Override
    public JsonObject toJson() {
        JsonObject rootJson = new JsonObject();

        JsonObject playersJson = new JsonObject();
        playersData.forEach((playerId, data) -> playersJson.add(playerId.toString(), data.toJson()));
        rootJson.add("players", playersJson);

        JsonObject entitiesJson = new JsonObject();
        entitiesData.forEach((entityId, data) -> entitiesJson.add(entityId.toString(), data.toJson()));
        rootJson.add("entities", entitiesJson);

        return rootJson;
    }

    @Override
    public void fromJson(JsonObject json) {
        clear();

        if (json.has("players")) {
            JsonObject playersJson = json.getAsJsonObject("players");
            playersJson.entrySet().forEach(entry -> {
                playersData.put(UUID.fromString(entry.getKey()), new ShopLimiterPlayerData(entry.getValue().getAsJsonObject()));
            });
        }

        if (json.has("entities")) {
            JsonObject entitiesJson = json.getAsJsonObject("entities");
            entitiesJson.entrySet().forEach(entry -> {
                entitiesData.put(UUID.fromString(entry.getKey()), new ShopLimiterEntityData(entry.getValue().getAsJsonObject()));
            });
        }
    }

    /**
     * Сериализует персональные данные конкретного игрока и общие глобальные лимиты
     * в сетевой буфер для отправки на клиент.
     *
     * @param player UUID игрока, данные которого необходимо отправить
     * @param buf Сетевой буфер для записи
     */
    @Override
    public void toNetwork(UUID player, FriendlyByteBuf buf) {
        getPlayerData(player).toNetwork(buf);

        buf.writeVarInt(entitiesData.size());
        entitiesData.forEach((entityId, data) -> data.toNetwork(buf));
    }

    @Override
    public void fromNetwork(FriendlyByteBuf buf) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void load() {
        if (!Files.exists(saveFile)) {
            LOGGER.info("Shop limiter data file not found. Starting fresh.");
            return;
        }

        try (Reader reader = Files.newBufferedReader(saveFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            fromJson(json);
            LOGGER.info("Loaded shop limiter data.");
        } catch (Exception e) {
            LOGGER.error("Error loading shop limiter data from: {}", saveFile, e);
        }
    }

    @Override
    public void save() {
        try {
            if (saveFile.getParent() != null) {
                Files.createDirectories(saveFile.getParent());
            }

            try (Writer writer = Files.newBufferedWriter(saveFile)) {
                GSON_LOCAL.get().toJson(this.toJson(), writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save shop limiter data", e);
        }
    }

    /**
     * Асинхронно сохраняет текущее состояние всех лимитов в файл.
     * Выполняется в отдельном пуле потоков (ioExecutor), не блокируя основной поток сервера.
     */
    @Override
    public void saveAsync() {
        ioExecutor.submit(this::save);
    }

    /**
     * Синхронно сохраняет данные и корректно завершает работу пула потоков.
     * Этот метод должен обязательно вызываться при остановке сервера (ServerStoppingEvent),
     * чтобы предотвратить потерю данных о покупках.
     */
    @Override
    public void shutdown() {
        save();
        ioExecutor.shutdown();
    }

    public static class Manager implements ServerOperation, ThreadingOperationTimeSave {
        @Override
        public void onServerStart(MinecraftServer server) {
            new ShopLimiterTableServer(server, true);
        }

        @Override
        public void onServerStop(MinecraftServer server) {
            getInstance().shutdown();
        }

        @Override
        public void onReload() { }

        @Override
        public void onDataStartSave() {
            getInstance().saveAsync();
        }

        @Override
        public int getDataSaveTimeSeconds() {
            final ShopConfig config = SDMShop2.getConfig();
            return config.autoSaveShopLimiterData ? config.saveShopLimiterDataIntervalSeconds : -1;
        }
    }
}
