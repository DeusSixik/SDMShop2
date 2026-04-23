package dev.sixik.sdmshop2.libs.shop.base.repositoryManager;

import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.base.ShopServerGetter;
import dev.sixik.sdmshop2.libs.shop.base.repository.Repository;
import lombok.Setter;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public abstract class RepositoryManager {

    @Setter
    protected ShopServerGetter serverGetter;

    public abstract void init();

    public abstract void close();

    public abstract <K, V> Repository<K, V> createRepository(@Nullable Path custom, @NotNull String name, RepoDefinition<K, V> def);
}
