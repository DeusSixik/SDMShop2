package dev.sixik.sdmshop2.libs.shop.builder;

import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.money.MoneyCostComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

/**
 * Билдер для создания предложений (офферов) в магазине.
 */
public class ShopOfferBuilder implements ShopEntityBuilder<ShopOfferBuilder> {

    /**
     * Создает новый экземпляр билдера со случайным UUID.
     * @param groupId ID группы (категории), к которой относится предложение.
     * @return Новый экземпляр ShopOfferBuilder.
     */
    public static ShopOfferBuilder builder(String groupId) {
        return new ShopOfferBuilder(UUID.randomUUID(), groupId);
    }

    /**
     * Создает новый экземпляр билдера с указанным UUID.
     * @param offerId Уникальный идентификатор предложения.
     * @param groupId ID группы (категории), к которой относится предложение.
     * @return Новый экземпляр ShopOfferBuilder.
     */
    public static ShopOfferBuilder builder(UUID offerId, String groupId) {
        return new ShopOfferBuilder(offerId, groupId);
    }

    @Getter
    protected final UUID offerId;

    @Getter
    protected final String groupId;

    @Getter
    private ObjectArrayList<ShopComponent> components = new ObjectArrayList<>();

    protected ShopOfferBuilder(UUID offerId, String groupId) {
        this.offerId = offerId;
        this.groupId = groupId;
    }

    /**
     * Добавляет цену в указанной валюте.
     * @param currencyId ID валюты.
     * @param amount Количество.
     * @return Текущий экземпляр билдера.
     */
    public ShopOfferBuilder addPrice(String currencyId, double amount) {
        return addPrice(currencyId, amount, "");
    }

    /**
     * Добавляет цену в указанной валюте с привязкой к группе (например, для скидок или условий).
     * @param currencyId ID валюты.
     * @param amount Количество.
     * @param groupId ID группы цены.
     * @return Текущий экземпляр билдера.
     */
    public ShopOfferBuilder addPrice(String currencyId, double amount, String groupId) {
        if (currencyId == null || currencyId.isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        final ResourceLocation id = currencyId.contains(":") ? ResourceLocation.tryParse(currencyId) : ResourceLocation.tryBuild("sdm", currencyId);
        final MoneyCostComponent component = new MoneyCostComponent(id, amount);
        component.setGroupId(groupId);
        components.add(component);
        return this;
    }

    /**
     * Завершает настройку предложения (используется для цепочки вызовов).
     * @return Текущий экземпляр билдера.
     */
    public ShopOfferBuilder end() {
        return this;
    }
}
