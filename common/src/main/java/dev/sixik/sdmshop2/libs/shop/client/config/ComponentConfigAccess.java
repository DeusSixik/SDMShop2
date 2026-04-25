package dev.sixik.sdmshop2.libs.shop.client.config;

import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.annotation.ComponentConfig;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ComponentConfigAccess {

    private static final Map<Class<?>, ObjectArrayList<CachedField>> CACHE = new Object2ObjectOpenHashMap<>();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    public static ObjectArrayList<CachedField> getCachedFields(Class<? extends ShopComponent> clazz) {
        return CACHE.computeIfAbsent(clazz, ComponentConfigAccess::buildCacheFor);
    }

    private static ObjectArrayList<CachedField> buildCacheFor(Class<?> originalClazz) {
        ObjectArrayList<CachedField> syncableFields = new ObjectArrayList<>();

        Class<?> currentClazz = originalClazz;

        while (currentClazz != null && currentClazz != Object.class) {

            for (Field field : currentClazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(ComponentConfig.class)) {
                    ComponentConfig meta = field.getAnnotation(ComponentConfig.class);
                    try {
                        field.setAccessible(true);

                        MethodHandle getter = LOOKUP.unreflectGetter(field);
                        MethodHandle setter = LOOKUP.unreflectSetter(field);

                        Class<?> mainType = field.getType();
                        Class<?> innerType = null;

                        Type genericType = field.getGenericType();
                        if (genericType instanceof ParameterizedType parameterizedType) {
                            Type[] typeArguments = parameterizedType.getActualTypeArguments();
                            if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?> innerClz) {
                                innerType = innerClz;
                            }
                        }

                        syncableFields.add(new CachedField(
                                meta.translationKey().isEmpty() ? field.getName() : meta.translationKey(),
                                mainType,
                                innerType,
                                meta.tips(),
                                getter,
                                setter
                        ));

                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to unreflect field " + field.getName() + " in class " + currentClazz.getName(), e);
                    }
                }
            }

            currentClazz = currentClazz.getSuperclass();
        }

        return syncableFields;
    }

    public record CachedField(String translationKey, Class<?> type, @Nullable Class<?> innerType, String[] tips, MethodHandle getter, MethodHandle setter) {}
}
