package dev.sixik.sdmshop2.libs.shop.components.promo.conditions;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTable;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.PromoComponent;
import dev.sixik.sdmshop2.utils.ShopUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class PromoCooldownComponent extends PromoComponent {

    public static final IComponentType<PromoCooldownComponent> TYPE = new Type();

    @Getter
    @Setter
    private long cooldownMs;

    public PromoCooldownComponent() {
        this(0L);
    }

    public PromoCooldownComponent(long cooldownMs) {
        this.cooldownMs = cooldownMs;
    }

    @Override
    public boolean isActive(Player player) {
        Optional<ShopLimiterTable> tableOpt = ShopUtils.getLimiterTable(player.isLocalPlayer());

        if (tableOpt.isEmpty()) {
            return false;
        }

        UUID offerId = ((ShopOffer) getRoots()).getUUID();

        long lastTime = tableOpt.get()
                .getPlayerData(player)
                .getData(offerId)
                .getLastPurchaseTime().get();

        return lastTime == 0 || (System.currentTimeMillis() - lastTime) >= cooldownMs;
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    private static class Type implements IComponentType<PromoCooldownComponent> {

        private static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "promo_cooldown");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public JsonObject serialize(PromoCooldownComponent component) {
            JsonObject json = new JsonObject();
            json.addProperty("cooldown_ms", component.cooldownMs);
            return json;
        }

        @Override
        public PromoCooldownComponent deserialize(JsonObject json) {
            return new PromoCooldownComponent(json.get("cooldown_ms").getAsLong());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PromoCooldownComponent component) {
            buf.writeLong(component.cooldownMs);
        }

        @Override
        public PromoCooldownComponent fromNetwork(FriendlyByteBuf buf) {
            return new PromoCooldownComponent(buf.readLong());
        }

        @Override
        public PromoCooldownComponent createDefault() {
            return new PromoCooldownComponent();
        }
    }
}
