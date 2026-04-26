package dev.sixik.sdmshop2.libs.shop.components.promo.effects;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.PromoEffectComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.annotation.ComponentConfig;
import dev.sixik.sdmshop2.libs.shop.components.api.annotation.ComponentNumberRange;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Set;

public class DiscountComponent extends PromoEffectComponent {

    public static final IComponentType<DiscountComponent> TYPE = new Type();

    @Getter
    @ComponentConfig(translationKey = "shop.component.promo.effects.discount.discount")
    @ComponentNumberRange(doubleMin = 0)
    private double discount;

    public DiscountComponent() {}

    public DiscountComponent(double discount) {
        this.discount = discount;
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    @Override
    public double applyPrice(double input, Set<String> activePromo, Set<String> activeGroups) {
        return input * (1 - discount);
    }

    private static class Type implements IComponentType<DiscountComponent> {

        private static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "discount");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public JsonObject serialize(DiscountComponent component) {
            JsonObject json = new JsonObject();
            json.addProperty("discount", component.discount);
            return json;
        }

        @Override
        public DiscountComponent deserialize(JsonObject json) {
            return new DiscountComponent(json.get("discount").getAsDouble());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, DiscountComponent component) {
            buf.writeDouble(component.discount);
        }

        @Override
        public DiscountComponent fromNetwork(FriendlyByteBuf buf) {
            return new DiscountComponent(buf.readDouble());
        }

        @Override
        public DiscountComponent createDefault() {
            return new DiscountComponent();
        }

        @Override
        public DiscountComponent createFromBuilder(Object... args) {
            if (args.length < 1 || !(args[0] instanceof Number value)) {
                throw new IllegalArgumentException("[DiscountComponent.createFromBuilder()] requires at least 1 number argument (discount amount).");
            }

            DiscountComponent component = new DiscountComponent(value.doubleValue());

            if (args.length >= 2 && args[1] instanceof String promoId) {
                component.setTargetPromoId(promoId);
            }

            for (int i = 2; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof String groupId) {
                    component.applyGroup(groupId);
                } else if (arg instanceof Collection<?> groups) {
                    groups.forEach(g -> component.applyGroup(String.valueOf(g)));
                } else {
                    throw new IllegalArgumentException("[DiscountComponent.createFromBuilder()] Invalid group format at index " + i);
                }
            }

            return component;
        }
    }
}
