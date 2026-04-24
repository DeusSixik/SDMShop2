package dev.sixik.sdmshop2.libs.shop.builder;

import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.misc.CatalogComponent;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class ShopBuilder implements ShopEntityBuilder<ShopBuilder> {

    public static ShopBuilder builder(ResourceLocation shopId) {
        return new ShopBuilder(shopId);
    }

    protected final ResourceLocation shopId;

    @Getter
    protected final ObjectArrayList<ShopComponent> components = new ObjectArrayList<>();

    protected final Object2ObjectOpenHashMap<String, UUID> categoriesMap = new Object2ObjectOpenHashMap<>();
    protected final ObjectArrayList<ShopOfferBuilder> offers = new ObjectArrayList<>();

    protected ShopBuilder(ResourceLocation shopId) {
        this.shopId = shopId;
    }

    public ShopBuilder addOffer(ShopOfferBuilder builder) {
        offers.add(builder);
        categoriesMap.put(builder.getGroupId(), builder.getOfferId());
        return this;
    }

    public ShopInstance end() {
        ShopInstance instance = ShopInstance.createManager(shopId, false);
        instance.setShouldSave(false);

        final var arrayComponents = components.elements();
        for (int i = 0; i < components.size(); i++) {
            instance.addComponent(arrayComponents[i]);
        }
        instance.initializeServerOnlyComponents();

        final ShopOfferBuilder[] arrayOffers = offers.elements();
        for (int i = 0; i < offers.size(); i++) {
            final ShopOfferBuilder offerBuilder = arrayOffers[i];

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
