package dev.sixik.sdmshop2.libs.sdmeconomy;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class SDMEconomyService {

    @Getter
    private static SDMEconomyService Instance;

    static SDMEconomyService init() {
        final SDMEconomyService service = new SDMEconomyService();
        Instance = service;
        return service;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SDMEconomyService.class);

    private final LoadingCache<UUID, BankAccount> accountCache;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    private final Path dataFolder;

    public SDMEconomyService() {
        this(SDMEconomyPlatform.getPlayersDataDir());
    }

    public SDMEconomyService(Path dataFolder) {
        this.dataFolder = dataFolder;
        this.accountCache = Caffeine.newBuilder()
                .expireAfterAccess(5, TimeUnit.MINUTES)
                .removalListener((uuid, account, cause) -> {
                    if(((BankAccount)account).isDirty())
                        saveAccount((UUID) uuid, (BankAccount) account);
                })
                .build(this::loadAccountFromDisk);
    }

    public BankAccount getAccount(UUID gameProfileId) {
        return accountCache.get(gameProfileId);
    }

    public void unloadPlayer(UUID gameProfileId) {
        accountCache.invalidate(gameProfileId);
    }

    public void saveAllDirty() {
        ioExecutor.submit(() -> {
            accountCache.asMap().forEach((gameProfileId, acc) -> {
                if(acc.isDirty()) saveAccount(gameProfileId, acc);
            });
        });
    }

    private void saveAccount(UUID gameProfileId, BankAccount account) {
        final Path pathToFile = dataFolder.resolve(getFileName(gameProfileId));
        final File file = pathToFile.toFile();

        try {
            NbtIo.write(account.serializeNbt(), file);
            account.markClean();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private BankAccount loadAccountFromDisk(UUID gameProfileId) {
        final Path pathToFile = dataFolder.resolve(getFileName(gameProfileId));
        final File file = pathToFile.toFile();

        if(file.exists()) {
            try {
                final CompoundTag nbt = NbtIo.read(file);

                final BankAccount account = new BankAccount(gameProfileId);
                account.deserializeNbt(nbt);
                return account;
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        return new BankAccount(gameProfileId);
    }

    private static String getFileName(Object name) {
        return name.toString() + ".nbt";
    }
}
