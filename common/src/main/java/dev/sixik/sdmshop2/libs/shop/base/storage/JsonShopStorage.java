package dev.sixik.sdmshop2.libs.shop.base.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class JsonShopStorage extends ShopStorage{

    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonShopStorage.class);


    @Override
    public void init() { }

    @Override
    public @Nullable ShopInstance load(ResourceLocation id) {
        try {
            final Path shopsDir = serverGetter.getShopsDir();

            Path shopPath = shopsDir.resolve(id.getPath() + ".json");

            if (!Files.isRegularFile(shopPath)) {
                return null;
            }

            try (Reader reader = Files.newBufferedReader(shopPath)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                return ShopInstance.fromJson(json);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load shop {}", id, e);
            return null;
        }
    }

    @Override
    public Map<ResourceLocation, ShopInstance> loadAll() {
        try {
            Object2ObjectMap<ResourceLocation, ShopInstance> loadedShops = new Object2ObjectOpenHashMap<>();
            final Path shopsDir = serverGetter.getShopsDir();

            if (!Files.exists(shopsDir)) {
                Files.createDirectories(shopsDir);
            }

            try (Stream<Path> files = Files.walk(shopsDir)) {
                files.filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .forEach(path -> loadShopFromFile(path, loadedShops));
            }
            return loadedShops;
        } catch (Exception e) {
            LOGGER.error("Failed to load shops", e);
            return Map.of();
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

    @Override
    public void save(ShopInstance shop) {
        try {
            final Path shopsDir = serverGetter.getShopsDir();
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

    @Override
    public void delete(ResourceLocation id) {
        try {
            final Path shopsDir = serverGetter.getShopsDir();
            Files.deleteIfExists(shopsDir.resolve(id.getPath() + ".json"));
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void close() { }
}
