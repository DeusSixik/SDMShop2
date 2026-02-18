package dev.sixik.sdmshop2.libs.shop.components.api.exceptions;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
