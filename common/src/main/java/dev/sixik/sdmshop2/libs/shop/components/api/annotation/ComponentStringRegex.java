package dev.sixik.sdmshop2.libs.shop.components.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ComponentStringRegex {

    /**
     * Регулярное выражение (например, "^[a-z0-9_]+:[a-z0-9_]+$" для ResourceLocation)
     */
    String value();

    /**
     * Сообщение об ошибке, если строка не прошла валидацию
     */
    String errorMessage() default "Invalid format";
}
