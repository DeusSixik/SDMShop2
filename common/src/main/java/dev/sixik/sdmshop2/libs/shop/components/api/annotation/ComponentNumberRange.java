package dev.sixik.sdmshop2.libs.shop.components.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ComponentNumberRange {
    double doubleMin() default -Double.MAX_VALUE;

    double doubleMax() default Double.MAX_VALUE;

    float floatMin() default -Float.MAX_VALUE;

    float floatMax() default Float.MAX_VALUE;

    int intMin() default Integer.MIN_VALUE;

    int intMax() default Integer.MAX_VALUE;

    long longMin() default Long.MIN_VALUE;

    long longMax() default Long.MAX_VALUE;
}
