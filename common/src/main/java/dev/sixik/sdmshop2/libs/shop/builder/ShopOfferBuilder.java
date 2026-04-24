package dev.sixik.sdmshop2.libs.shop.builder;

import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.money.MoneyCostComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ShopOfferBuilder implements ShopEntityBuilder<ShopOfferBuilder> {

    public static ShopOfferBuilder builder(String groupId) {
        return new ShopOfferBuilder(UUID.randomUUID(), groupId);
    }

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

    public ShopOfferBuilder addPrice(String currencyId, double amount) {
        return addPrice(currencyId, amount, "");
    }

    public ShopOfferBuilder addPrice(String currencyId, double amount, String groupId) {
        if (currencyId == null || currencyId.isEmpty()) {
            throw new IllegalArgumentException("Currency ID cannot be null or empty");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        ResourceLocation id = ResourceLocation.tryParse(currencyId);
        if(id == null)
            id = ResourceLocation.tryBuild("sdm", currencyId);

        final MoneyCostComponent component = new MoneyCostComponent(id, amount);
        component.setGroupId(groupId);
        components.add(component);
        return this;
    }

    public ShopOfferBuilder end() {
        return this;
    }
}
