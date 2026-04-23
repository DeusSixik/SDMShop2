package dev.sixik.sdmshop2.libs.shop.base.storage;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopServerGetter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public abstract class ShopStorage {

    @Setter
    protected ShopServerGetter serverGetter;

    /** Вызывается при старте сервера для коннекта к БД или создания папок */
    public abstract void init();

    /** Загружает один магазин по его ID. Возвращает null, если не найден. */
    @Nullable
    public abstract ShopInstance load(ResourceLocation id);

    /** Загружает все магазины */
    public abstract Map<ResourceLocation, ShopInstance> loadAll();

    /** Сохраняет один магазин */
    public abstract void save(ShopInstance shop);

    /** Удаляет магазин */
    public abstract void delete(ResourceLocation id);

    /** Закрывает соединения (вызывается при остановке сервера) */
    public abstract void close();
}
