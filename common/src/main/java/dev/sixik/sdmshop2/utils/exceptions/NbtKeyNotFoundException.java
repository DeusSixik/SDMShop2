package dev.sixik.sdmshop2.utils.exceptions;

public class NbtKeyNotFoundException extends RuntimeException {

    public NbtKeyNotFoundException(String key) {
        super("Key with name '" + key + "' not found!");
    }
}
