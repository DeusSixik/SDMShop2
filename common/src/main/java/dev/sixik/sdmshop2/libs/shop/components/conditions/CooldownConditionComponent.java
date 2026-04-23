package dev.sixik.sdmshop2.libs.shop.components.conditions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTable;
import dev.sixik.sdmshop2.libs.shop.components.api.ConditionComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.limiter.LimiterComponent;
import dev.sixik.sdmshop2.utils.ShopUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public class CooldownConditionComponent extends ConditionComponent {

    public static final IComponentType<CooldownConditionComponent> TYPE = new Type();

    @Getter
    @Setter
    private long cooldownMs;

    @Getter
    @Setter
    private LimiterComponent.LimiterType limiterType;

    public CooldownConditionComponent() {
        this(0L, LimiterComponent.LimiterType.Player);
    }

    public CooldownConditionComponent(long cooldownMs, LimiterComponent.LimiterType limiterType) {
        this.cooldownMs = cooldownMs;
        this.limiterType = limiterType;
    }

    @Override
    public boolean isChecked(Player player) {
        Optional<ShopLimiterTable> tableOpt = ShopUtils.getLimiterTable(player.isLocalPlayer());

        if (tableOpt.isEmpty()) {
            return false;
        }

        UUID offerId = ((ShopOffer) getRoots()).getUUID();
        ShopLimiterTable table = tableOpt.get();
        long lastTime = 0;

        if (limiterType == LimiterComponent.LimiterType.Player) {
            lastTime = table.getPlayerData(player).getData(offerId).getLastPurchaseTime().get();
        } else {
            lastTime = table.getOfferDatga(offerId).getLastPurchaseTime().get();
        }

        return lastTime == 0 || (System.currentTimeMillis() - lastTime) >= cooldownMs;
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    private static class Type implements IComponentType<CooldownConditionComponent> {

        private static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "condition_cooldown");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public JsonObject serialize(CooldownConditionComponent component) {
            JsonObject json = new JsonObject();
            json.addProperty("cooldown_ms", component.cooldownMs);
            json.addProperty("limiter_type", component.limiterType.name());
            return json;
        }

        @Override
        public CooldownConditionComponent deserialize(JsonObject json) {
            if (!json.has("cooldown_ms")) {
                throw new JsonParseException("[CooldownConditionComponent] Missing 'cooldown_ms'");
            }

            long cooldown = json.get("cooldown_ms").getAsLong();
            LimiterComponent.LimiterType type = LimiterComponent.LimiterType.Player; // По умолчанию

            if (json.has("limiter_type")) {
                String typeStr = json.get("limiter_type").getAsString();
                try {
                    type = LimiterComponent.LimiterType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    throw new JsonParseException("[CooldownConditionComponent] Invalid 'type'. Expected: " + Arrays.toString(LimiterComponent.LimiterType.values()));
                }
            }

            return new CooldownConditionComponent(cooldown, type);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, CooldownConditionComponent component) {
            buf.writeLong(component.cooldownMs);
            buf.writeEnum(component.limiterType);
        }

        @Override
        public CooldownConditionComponent fromNetwork(FriendlyByteBuf buf) {
            return new CooldownConditionComponent(buf.readLong(), buf.readEnum(LimiterComponent.LimiterType.class));
        }

        @Override
        public CooldownConditionComponent createDefault() {
            return new CooldownConditionComponent();
        }
    }
}
