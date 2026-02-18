package dev.sixik.sdmshop2.libs.sdmeconomy;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Класс в которм храняться данные о всех игроках и их балансах. <br> <br>
 * <p>Основной принцип работы такой что если игрока в течении 10 минут не используют он будет выгружен на диск
 * , но если он понадобиться он будет обратно загружен</p>
 */
public class SDMEconomyService {

    public static final Logger LOGGER = LoggerFactory.getLogger(SDMEconomyService.class);

    @Getter
    private static SDMEconomyService Instance;

    public static SDMEconomyService init() {
        return init(new SDMEconomyService());
    }

    public static SDMEconomyService init(SDMEconomyService service) {
        Instance = service;
        return service;
    }

    protected final LoadingCache<UUID, BankAccount> accountCache;
    protected final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    protected final Path dataFolder;

    public SDMEconomyService() {
        this(SDMEconomyPlatform.getPlayersDataDir());
    }

    public SDMEconomyService(Path dataFolder) {
        this.dataFolder = dataFolder;
        this.accountCache = Caffeine.newBuilder()
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .removalListener((uuid, account, cause) -> {
                    if(((BankAccount)account).isDirty())
                        saveAccount((UUID) uuid, (BankAccount) account);
                })
                .build(this::loadAccountFromDisk);
    }

    /**
     * Получает данные игрока с его валютами. <br>
     * <p>Если игрока нет в кэше он будет загружен с диска</p>
     * @param gameProfileId {@link com.mojang.authlib.GameProfile}
     */
    public BankAccount getAccount(UUID gameProfileId) {
        return accountCache.get(gameProfileId);
    }

    /**
     * Выгружает игрока из памяти на диск если он уже не нужно
     * @param gameProfileId {@link com.mojang.authlib.GameProfile}
     */
    public void unloadPlayer(UUID gameProfileId) {
        accountCache.invalidate(gameProfileId);
    }

    /**
     * Сохраняет все Аккаунты нуждающиеся в этом
     */
    public void saveAllDirty() {
        ioExecutor.submit(() -> {
            accountCache.asMap().forEach((gameProfileId, acc) -> {
                if(acc.isDirty()) saveAccount(gameProfileId, acc);
            });
        });
    }

    protected void saveAccount(UUID gameProfileId, BankAccount account) {
        final Path pathToFile = dataFolder.resolve(getFileName(gameProfileId));
        final File file = pathToFile.toFile();

        try {
            NbtIo.write(account.serializeNbt(), file);
            account.markClean();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    protected BankAccount loadAccountFromDisk(UUID gameProfileId) {
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

    protected static String getFileName(Object name) {
        return name.toString() + ".nbt";
    }
}
