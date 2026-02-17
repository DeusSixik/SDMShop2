package dev.sixik.sdmshop2.libs.sdmeconomy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class SDMEconomyCurrencyRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(SDMEconomyCurrencyRegistry.class);

    private static final Map<ResourceLocation, ICurrencyType<?>> TYPES = new HashMap<>();

    private static final Map<ResourceLocation, IExternalCurrency> CURRENCIES = new HashMap<>();

    public static void registerType(ResourceLocation id, ICurrencyType<?> type) {
        TYPES.put(id, type);
    }

    public static IExternalCurrency getCurrency(String id) {
        return getCurrency(
                        id.contains(":") ?
                        ResourceLocation.tryParse(id) :
                        ResourceLocation.tryBuild("sdm", id)
        );
    }

    public static IExternalCurrency getCurrency(ResourceLocation id) {
        return CURRENCIES.get(id);
    }

    public static void reload() {
        reload(SDMEconomyPlatform.getCurrenciesDir());
    }

    public static void reload(Path configDir) {
        CURRENCIES.clear();

        final File file = configDir.toFile();

        if(!file.exists()) {
            LOGGER.error("Can't reload currencies because, currencies folder not exists!");
            return;
        }

        final File[] listFiles = file.listFiles();

        for (int i = 0; i < listFiles.length; i++) {
            loadCurrency(listFiles[i]);
        }
    }

    private static void loadCurrency(File file) {
        final String extension = FilenameUtils.getExtension(file.getName());
        if (!extension.equals("json")) {
            LOGGER.warn("Skipping file '{}': Not a JSON", file.getName());
            return;
        }

        final String fileName = FilenameUtils.removeExtension(file.getName());
        final ResourceLocation currencyId = new ResourceLocation("sdm", fileName);

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            final JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

            if (!json.has("type")) {
                LOGGER.error("Failed to load currency '{}': Missing 'type' field", fileName);
                return;
            }

            final String typeStr = json.get("type").getAsString();
            final ResourceLocation typeId = new ResourceLocation(typeStr);

            final ICurrencyType<?> typeFactory = SDMEconomyCurrencyRegistry.TYPES.get(typeId);

            if (typeFactory == null) {
                LOGGER.error("Unknown currency type '{}' in file '{}'", typeId, file.getName());
                return;
            }

            final IExternalCurrency currency = typeFactory.deserialize(currencyId, json);

            CURRENCIES.put(currencyId, currency);
            LOGGER.info("Loaded currency: {}", currencyId);
        } catch (Exception e) {
            LOGGER.error("Error loading currency from file '{}'", file.getName(), e);
        }
    }
}
