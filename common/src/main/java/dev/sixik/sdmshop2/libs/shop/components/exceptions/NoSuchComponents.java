package dev.sixik.sdmshop2.libs.shop.components.exceptions;

public class NoSuchComponents extends RuntimeException {

    public NoSuchComponents(Class<?> own, Class<?> clz) {
        super("Component: '" + own.getName() + "' required '" + clz.getName() + "' component!");
    }
}
