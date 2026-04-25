package dev.sixik.sdmshop2.libs.shop.components.limiter;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterOfferData;
import dev.sixik.sdmshop2.libs.shop.base.limiter.ShopLimiterTable;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.utils.ShopUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Arrays;
import java.util.UUID;

public class LimiterComponent extends ShopComponent {

    public static final IComponentType<LimiterComponent> TYPE = new Type();

    public enum LimiterType {
        World, // Лимит у всех игроков един
        Player // Лимит только у конкретного игрока
    }

    private UUID rootId;

    @Getter
    @Setter
    private LimiterType limiterType;

    @Getter
    @Setter
    private int count;

    @Getter
    @Setter
    private long resetIntervalMs;

    public LimiterComponent() {
        this(LimiterType.World, 1, 0L);
    }

    public LimiterComponent(LimiterType type, int count) {
        this(type, count, 0L);
    }

    public LimiterComponent(LimiterType type, int count, long resetIntervalMs) {
        this.limiterType = type;
        this.count = count;
        this.resetIntervalMs = resetIntervalMs;
    }

    @Override
    public void init() {
        final ShopEntity root = getRoot();

        if(root != null) {
            this.rootId = ((ShopOffer) root).getUUID();
        } else {
            throw new RuntimeException("[LimiterComponent] Root entity is missing!");
        }
    }

    public boolean isChecked(Player player, int purchaseAmount) {
        final ShopLimiterTable limiterTable = ShopUtils.getLimiterTable(player.isLocalPlayer()).orElse(null);
        if (limiterTable == null) return false;

        ShopLimiterOfferData data = (limiterType == LimiterType.Player)
                ? limiterTable.getPlayerData(player).getData(this.rootId)
                : limiterTable.getOfferDatga(this.rootId);

        int currentPurchases = data.getCount().get();
        long lastTime = data.getLastPurchaseTime().get();

        /*
            Если задан интервал и время ожидания вышло - виртуально "обнуляем" текущие покупки для проверки
         */
        if (this.resetIntervalMs > 0 && lastTime > 0) {
            if ((System.currentTimeMillis() - lastTime) >= this.resetIntervalMs) {
                currentPurchases = 0;
            }
        }

        return (currentPurchases + purchaseAmount) <= this.count;
    }

    /**
     * Увеличивает счетчик покупок на указанное значение.
     */
    public void addLimit(Player player, int amount) {
        final ShopLimiterTable limiterTable = ShopUtils.getLimiterTable(false).get();

        ShopLimiterOfferData data = (limiterType == LimiterType.Player)
                ? limiterTable.getPlayerData(player).getData(this.rootId)
                : limiterTable.getOfferDatga(this.rootId);

        long lastTime = data.getLastPurchaseTime().get();

        if (this.resetIntervalMs > 0 && lastTime > 0 && (System.currentTimeMillis() - lastTime) >= this.resetIntervalMs) {
            data.set(amount);
            data.markPurchased();
        } else {
            data.add(amount);
        }
    }

    /**
     * Уменьшает счетчик покупок на указанное значение.
     */
    public void minusLimit(Player player, int amount) {
        final ShopLimiterTable limiterTable = ShopUtils.getLimiterTable(false).get();

        if (limiterType == LimiterType.Player) {
            limiterTable.getPlayerData(player).getData(this.rootId).safeMinus(amount);
        } else {
            limiterTable.getOfferDatga(this.rootId).safeMinus(amount);
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
            limiterTable.getOfferDatga(this.rootId).set(amount);
        }
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
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
            json.addProperty("limiter_type", component.limiterType.name());
            json.addProperty("count", component.count);

            if (component.resetIntervalMs > 0) {
                json.addProperty("reset_interval_ms", component.resetIntervalMs);
            }
            return json;
        }

        @Override
        public LimiterComponent deserialize(JsonObject json) {
            if (!json.has("type")) {
                throw new JsonParseException("[LimiterComponent] Missing required parameter: 'type'");
            }
            if (!json.has("count")) {
                throw new JsonParseException("[LimiterComponent] Missing required parameter: 'count'");
            }

            LimiterType type;
            String typeStr = json.get("limiter_type").getAsString();
            try {
                type = LimiterType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("[LimiterComponent] Invalid 'type': '" + typeStr +
                        "'. Expected one of: " + Arrays.toString(LimiterType.values()));
            }

            try {
                int count = json.get("count").getAsInt();

                long resetInterval = json.has("reset_interval_ms") ? json.get("reset_interval_ms").getAsLong() : 0L;

                return new LimiterComponent(type, count, resetInterval);
            } catch (NumberFormatException | UnsupportedOperationException e) {
                throw new JsonParseException("[LimiterComponent] Parameter 'count' or 'reset_interval_ms' must be a valid number!");
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, LimiterComponent component) {
            buf.writeEnum(component.limiterType);
            buf.writeInt(component.count);
            buf.writeLong(component.resetIntervalMs);
        }

        @Override
        public LimiterComponent fromNetwork(FriendlyByteBuf buf) {
            LimiterType type = buf.readEnum(LimiterType.class);
            int count = buf.readInt();
            long resetInterval = buf.readLong();
            return new LimiterComponent(type, count, resetInterval);
        }

        @Override
        public LimiterComponent createDefault() {
            return new LimiterComponent();
        }

        @Override
        public LimiterComponent createFromBuilder(Object... args) {
            if(args.length != 2 && args.length != 3)
                throw new IllegalArgumentException("LimiterComponent.createFromBuilder() takes 2 or 3 arguments (LimiterType, int, (Optional) long)");

            final String type = (String) args[0];
            final int count = (int) args[1];

            if(args.length == 2)
                return new LimiterComponent(LimiterType.valueOf(type), count);

            final long resetInterval = (long) args[2];
            return new LimiterComponent(LimiterType.valueOf(type), count, resetInterval);
        }
    }
}
