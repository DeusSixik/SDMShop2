package dev.sixik.sdmshop2.libs.shop.base;

import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

public interface ShopServerGetter {

    Path getShopDirWorld();

    Path getShopDirConfig();

    Path getShopsDir();

    MinecraftServer getServer();
}
