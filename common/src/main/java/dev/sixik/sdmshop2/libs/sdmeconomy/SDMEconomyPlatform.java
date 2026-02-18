package dev.sixik.sdmshop2.libs.sdmeconomy;

import com.google.gson.Gson;
import dev.sixik.sdmshop2.libs.sdmeconomy.custom_currency.ExternalItemCurrency;
import dev.sixik.sdmshop2.libs.sdmeconomy.network.packets.SendDynamicCurrencyS2C;
import dev.sixik.sdmshop2.libs.sdmeconomy.network.packets.SendPlayerAccountS2C;
import dev.sixik.sdmshop2.utils.exceptions.NotInitializedException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SDMEconomyPlatform {

    @Nullable
    public static MinecraftServer server;

    public static final String MODID = "sdmeconomy";

    private static final Logger LOGGER = LoggerFactory.getLogger(SDMEconomyPlatform.class);

    public static final Gson GSON = new Gson();

    private static Path CONFIG_DIR;
    private static Path CURRENCIES_DIR;
    private static Path PLAYERS_DATA_DIR;

    public static void loadConfigDir(Path path) {
        CONFIG_DIR = resolveSdmDir(path, "economy");
        CURRENCIES_DIR = resolveSdmDir(path, "economy/currencies");
    }

    public static void loadPlayersDataDir(Path path) {
        PLAYERS_DATA_DIR = resolveSdmDir(path, "economy/players");
    }

    public static Path getConfigDir() {
        if(CONFIG_DIR == null)
            throw new NotInitializedException("CONFIG_DIR");

        return CONFIG_DIR;
    }

    public static Path getPlayersDataDir() {
        if(PLAYERS_DATA_DIR == null)
            throw new NotInitializedException("PLAYERS_DATA_DIR");

        return PLAYERS_DATA_DIR;
    }

    public static Path getCurrenciesDir() {
        if(CURRENCIES_DIR == null)
            throw new NotInitializedException("CURRENCIES_DIR");

        return CURRENCIES_DIR;
    }

    public static Path resolveSdmDir(Path rootPath, String subFolder) {
        Path cleanPath = rootPath.normalize();

        Path targetDir;
        if (cleanPath.endsWith("sdm")) {
            targetDir = cleanPath.resolve(subFolder);
        } else {
            targetDir = cleanPath.resolve("sdm").resolve(subFolder);
        }

        try {
            Files.createDirectories(targetDir);
        } catch (IOException e) {
            LOGGER.error("Can't create folder: {}", targetDir, e);
            throw new RuntimeException("Can't create folder: " + targetDir, e);
        }

        return targetDir;
    }

    public static void init() {
        SDMEconomyCurrencyRegistry.registerType(ResourceLocation.tryBuild("minecraft", "item"), new ExternalItemCurrency.ExternalItemCurrencyType());
        shutdownHook();
    }

    public static void onServerStart(MinecraftServer server) {
        SDMEconomyPlatform.server = server;
        loadPlayersDataDir(server.getWorldPath(LevelResource.ROOT));
        SDMEconomyService.init();
    }

    public static void onServerStop(MinecraftServer server) {
        SDMEconomyService.getInstance().saveAllDirty();
        SDMEconomyPlatform.server = null;
    }

    public static void onPlayerLeft(ServerPlayer player) {
        SDMEconomyService.getInstance().unloadPlayer(player.getGameProfile().getId());
    }

    public static void shutdownHook() {
        final Thread thread = new Thread(() -> {
            SDMEconomyService.getInstance().saveAllDirty();
            SDMEconomyPlatform.server = null;
        });
        thread.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(thread);
    }

    public static void onReload() {
        SDMEconomyCurrencyRegistry.reload();

        if(server == null) return;
        final CompoundTag nbt = SDMEconomyCurrencyRegistry.serializeCurrencies();
        final SendDynamicCurrencyS2C packet = new SendDynamicCurrencyS2C(nbt);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            packet.sendTo(player);
        }
    }

    public static void onPlayerJoin(ServerPlayer player) {
        new SendDynamicCurrencyS2C().sendTo(player);
        new SendPlayerAccountS2C(player).sendTo(player);
    }
}
