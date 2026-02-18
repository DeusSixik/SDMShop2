package dev.sixik.sdmshop2.libs.shop.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyPlatform;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public final class ShopTable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopTable.class);

    public static ShopTable Instance;

    private final ThreadLocal<Gson> GSON_LOCAL = ThreadLocal.withInitial(() -> new GsonBuilder().setPrettyPrinting().create());
    private final ExecutorService ioExecutor;
    private final ConcurrentHashMap<ResourceLocation, ShopInstance> shops = new ConcurrentHashMap<>();

    @Getter
    private final Path shopDirWorld;

    @Getter
    private final Path shopDirConfig;

    @Getter
    private final Path shopsDir;

    @Getter
    private final MinecraftServer server;

    private volatile boolean reloading = false;

    public ShopTable(MinecraftServer server) {
        this.ioExecutor = Executors.newSingleThreadExecutor();
        this.server = server;
        this.shopDirWorld = SDMEconomyPlatform.resolveSdmDir(server.getWorldPath(LevelResource.ROOT), "shop");
        this.shopDirConfig = SDMEconomyPlatform.resolveSdmDir(Platform.getConfigFolder(), "shop");
        this.shopsDir = SDMEconomyPlatform.resolveSdmDir(Platform.getConfigFolder(), "shop/shops");

        reload();
    }

    public void addShop(ResourceLocation shopId) {
        shops.put(shopId, ShopInstance.createManager(shopId, true));
    }

    public void addShop(ShopInstance instance) {
        shops.put(instance.getId(), instance);
    }

    @Nullable
    public ShopInstance getShop(ResourceLocation id) {
        return shops.get(id);
    }

    public Collection<ShopInstance> getAllShops() {
        return shops.values();
    }

    public void deleteShop(ShopInstance instance) {
        deleteShop(instance.getId());
    }

    public void deleteShop(ResourceLocation id) {
        ShopInstance removed = shops.remove(id);
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

    public void saveAll() {
        for (ShopInstance value : shops.values()) {
            save(value);
        }
    }

    public void saveAllAsync() {
        for (ShopInstance value : shops.values()) {
            saveAsync(value);
        }
    }

    public void save(ShopInstance instance) {
        saveShopToFile(instance);
    }

    public void saveAsync(ShopInstance instance) {
        ioExecutor.submit(() -> saveShopToFile(instance));
    }

    public void reload() {
        if(reloading) return;

        reloading = true;
        try {
            LOGGER.info("Start reloading shops data!");

            shops.clear();

            if (!Files.exists(shopsDir)) {
                try {
                    Files.createDirectories(shopsDir);
                } catch (IOException e) {
                    LOGGER.error("Can't create shops dir. {}", e.getMessage(), e);
                    return;
                }
            }

            try (Stream<Path> files = Files.walk(shopsDir)) {
                files.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(this::loadShopFromFile);
            } catch (IOException e) {
                LOGGER.error("Failed to load shops", e);
            }

            LOGGER.info("Loaded {} shops.", shops.size());
        } finally {
            reloading = false;
        }
    }

    private void loadShopFromFile(Path path) {
        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            final ShopInstance shopIds = ShopInstance.fromJson(json);
            shops.put(shopIds.getId(), shopIds);
        } catch (Exception e) {
            LOGGER.error("Error loading shop: " + path, e);
        }
    }

    private void saveShopToFile(ShopInstance shop) {
        try {
            Path path = shopsDir.resolve(shop.getId().getPath() + ".json");

            if (shop.getId().getPath().contains("/")) {
                Files.createDirectories(path.getParent());
            }

            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON_LOCAL.get().toJson(shop.serialize(), writer);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to save shop {}", shop.getId(), e);
        }
    }
}
