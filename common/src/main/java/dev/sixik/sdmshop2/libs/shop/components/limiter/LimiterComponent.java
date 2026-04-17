package dev.sixik.sdmshop2.libs.shop.components.limiter;

import com.google.gson.JsonObject;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.base.ObjectIdGetter;
import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterEntityData;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterPlayerData;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTable;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTableClient;
import dev.sixik.sdmshop2.libs.shop.components.api.ConditionComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.utils.ShopUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.UUID;

public class LimiterComponent extends ShopComponent {

    public static final IComponentType<LimiterComponent> TYPE = new Type();

    private UUID rootId;

    @Getter
    @Setter
    private LimiterType limiterType;

    @Getter
    @Setter
    private int count;

    public LimiterComponent() {
        this(LimiterType.World, 1);
    }

    public LimiterComponent(LimiterType type, int count) {
        this.limiterType = type;
        this.count = count;
    }

    @Override
    public void init() {
        final ShopEntity root = getRoot();

        if(root instanceof ObjectIdGetter objectIdGetter)
            this.rootId = objectIdGetter.getUUID();
        else {
            throw new RuntimeException("[LimiterComponent] Root entity must implement ObjectIdGetter!");
        }
    }

    public boolean isChecked(Player player) {
        final Optional<ShopLimiterTable> limiterTableOpt = ShopUtils.getLimiterTable(player.isLocalPlayer());

        if(limiterTableOpt.isEmpty()) {
            final String side = player.isLocalPlayer() ? "Client" : "Server";
            SDMShop2.LOGGER.error("LimiterTable is not initialized! Side: {}", side);
            return false;
        }

        final ShopLimiterTable limiterTable = limiterTableOpt.get();
        int currentPurchases;

        if (limiterType == LimiterType.Player) {
            currentPurchases = limiterTable.getPlayerData(player).get(this.rootId);
        } else {
            currentPurchases = limiterTable.getEntityData(this.rootId).getCount().get();
        }

        // Покупка разрешена только если текущее число покупок СТРОГО МЕНЬШЕ установленного лимита (this.count)
        return currentPurchases >= 0 && currentPurchases < this.count;
    }

    /**
     * Увеличивает счетчик покупок на указанное значение.
     */
    public void addLimit(Player player, int amount) {
        final ShopLimiterTable limiterTable = ShopUtils.getLimiterTable(false).get();

        if (limiterType == LimiterType.Player) {
            limiterTable.getPlayerData(player).getData(this.rootId).add(amount);
        } else {
            limiterTable.getEntityData(this.rootId).add(amount);
        }
    }

    /**
     * Уменьшает счетчик покупок на указанное значение.
     * Рекомендуется использовать safeMinus, если вы добавили его защиту от отрицательных чисел.
     */
    public void minusLimit(Player player, int amount) {
        final ShopLimiterTable limiterTable = ShopUtils.getLimiterTable(false).get();

        if (limiterType == LimiterType.Player) {
            limiterTable.getPlayerData(player).getData(this.rootId).safeMinus(amount);
        } else {
            limiterTable.getEntityData(this.rootId).safeMinus(amount);
        }
    }

    /**
     * Жестко устанавливает новое значение счетчика покупок.
     */
    public void setLimit(Player player, int amount) {
        final ShopLimiterTable limiterTable = ShopUtils.getLimiterTable(false).get();

        if (limiterType == LimiterType.Player) {
            limiterTable.getPlayerData(player).getData(this.rootId).set(amount);
        } else {
            limiterTable.getEntityData(this.rootId).set(amount);
        }
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    public enum LimiterType {
        World,
        Player
    }

    private static class Type implements IComponentType<LimiterComponent> {

        private static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "condition_limiter");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public JsonObject serialize(LimiterComponent component) {
            JsonObject json = new JsonObject();
            json.addProperty("type", component.limiterType.name());
            json.addProperty("count", component.count);
            return json;
        }

        @Override
        public LimiterComponent deserialize(JsonObject json) {
            if(json.has("type") && json.has("count")) {
                LimiterType type = LimiterType.valueOf(json.get("type").getAsString());
                int count = json.get("count").getAsInt();
                return new LimiterComponent(type, count);
            }
            throw new NullPointerException("Param with id 'type' or 'count' not exists!");
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, LimiterComponent component) {
            buf.writeEnum(component.limiterType);
            buf.writeInt(component.count);
        }

        @Override
        public LimiterComponent fromNetwork(FriendlyByteBuf buf) {
            LimiterType type = buf.readEnum(LimiterType.class);
            int count = buf.readInt();
            return new LimiterComponent(type, count);
        }

        @Override
        public LimiterComponent createDefault() {
            return new LimiterComponent();
        }
    }
}
