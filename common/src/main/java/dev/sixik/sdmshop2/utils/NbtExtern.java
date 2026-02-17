package dev.sixik.sdmshop2.utils;

import dev.sixik.sdmshop2.utils.exceptions.NbtKeyNotFoundException;
import net.minecraft.nbt.CompoundTag;

public class NbtExtern {

    public static <T> T getOrThrow(CompoundTag nbt, String key) {
        throwIsNoKey(nbt, key);
        return (T) nbt.get(key);
    }

    public static void throwIsNoKey(CompoundTag nbt, String key) {
        if(!nbt.contains(key))
            throw new NbtKeyNotFoundException(key);
    }
}
