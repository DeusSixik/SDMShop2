package dev.sixik.sdmshop2.libs.platform;

import net.minecraft.server.MinecraftServer;

public interface ServerOperation {

    default void onServerStart(MinecraftServer server) { }

    default void onServerStop(MinecraftServer server) { }

    default void onReload() { }

}
