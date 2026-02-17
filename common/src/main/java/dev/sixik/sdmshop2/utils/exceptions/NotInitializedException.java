package dev.sixik.sdmshop2.utils.exceptions;

public class NotInitializedException extends RuntimeException {

    public NotInitializedException(String message) {
        super("'" + message + "' not initialized!");
    }
}
