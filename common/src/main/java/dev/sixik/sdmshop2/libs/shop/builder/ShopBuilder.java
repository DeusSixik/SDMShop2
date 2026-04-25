package dev.sixik.sdmshop2.libs.shop.builder;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.CatalogComponent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Основной билдер для создания магазина.
 */
public class ShopBuilder implements ShopEntityBuilder<ShopBuilder> {

    /**
     * Создает новый экземпляр билдера магазина.
     * @param shopId Уникальный ID магазина.
     * @return Экземпляр ShopBuilder.
     */
    public static ShopBuilder builder(ResourceLocation shopId) {
        return new ShopBuilder(shopId);
    }

    protected final ResourceLocation shopId;

    @Getter
    protected final ObjectArrayList<ShopComponent> components = new ObjectArrayList<>();

    protected final Object2ObjectOpenHashMap<String, UUID> categoriesMap = new Object2ObjectOpenHashMap<>();
    protected final ObjectArrayList<ShopOfferBuilder> offers = new ObjectArrayList<>();
    protected boolean shouldSave = false;

    protected ShopBuilder(ResourceLocation shopId) {
        this.shopId = shopId;
    }

    /**
     * Создает и настраивает новое предложение в магазине.
     * @param groupId ID группы (категории).
     * @param configurator Лямбда-выражение для настройки предложения.
     * @return Текущий экземпляр ShopBuilder.
     */
    public ShopBuilder addOffer(String groupId, Consumer<ShopOfferBuilder> configurator) {
        ShopOfferBuilder offerBuilder = ShopOfferBuilder.builder(groupId);
        configurator.accept(offerBuilder);
        return this.addOffer(offerBuilder);
    }

    /**
     * Добавляет уже созданный билдер предложения.
     * @param builder Билдер предложения.
     * @return Текущий экземпляр ShopBuilder.
     */
    public ShopBuilder addOffer(ShopOfferBuilder builder) {
        offers.add(builder);
        categoriesMap.computeIfAbsent(builder.getGroupId(), k -> UUID.randomUUID());
        return this;
    }

    public ShopBuilder mustSave() {
        this.shouldSave = true;
        return this;
    }

    /**
     * Завершает сборку магазина и создает объект ShopInstance.
     * @return Настроенный экземпляр ShopInstance.
     */
    public ShopInstance end() {
        ShopInstance instance = ShopInstance.createManager(shopId, false);
        instance.setShouldSave(shouldSave);

        final Object[] arrayComponents = components.elements();
        for (int i = 0; i < components.size(); i++) {
            instance.addComponent((ShopComponent)arrayComponents[i]);
        }
        instance.initializeServerOnlyComponents();

        final Object[] arrayOffers = offers.elements();
        for (int i = 0; i < offers.size(); i++) {
            final ShopOfferBuilder offerBuilder = (ShopOfferBuilder) arrayOffers[i];

            ShopOffer offer = ShopOffer.create(offerBuilder.offerId, false);
            offer.addComponent(new CatalogComponent(offerBuilder.groupId, categoriesMap.getOrDefault(offerBuilder.groupId, UUID.randomUUID())));
            offerBuilder.getComponents().forEach(offer::addComponent);
            offer.initializeServerOnlyComponents();
            instance.getEntries().addEntry(offer);
        }
        instance.getCategories().reindex();
        return instance;
    }
}
