package dev.sixik.sdmshop2.libs.shop.components.promo.conditions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.PromoComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.Arrays;

public class PromoTimeComponent extends PromoComponent {

    public static final IComponentType<PromoTimeComponent> TYPE = new Type();

    public enum TimeMode {
        REAL_TIME_EPOCH, // Реальные миллисекунды (System.currentTimeMillis)
        SERVER_TICKS,    // Общее время жизни мира в тиках
        DAY_TIME         // Игровое время суток (0 - 24000)
    }

    @Getter
    @Setter
    private TimeMode mode;

    // Начало и конец акции (в зависимости от режима это мс или тики)
    @Getter
    @Setter
    private long startTime;

    @Getter
    @Setter
    private long endTime;

    public PromoTimeComponent() {
        this(TimeMode.REAL_TIME_EPOCH, 0, 0);
    }

    public PromoTimeComponent(TimeMode mode, long startTime, long endTime) {
        this.mode = mode;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isActive(MinecraftServer server) {
        long currentTime = 0;

        switch (mode) {
            case REAL_TIME_EPOCH:
                currentTime = System.currentTimeMillis();
                break;
            case SERVER_TICKS:
                // Берем главный мир (Overworld) как эталон времени сервера
                ServerLevel level = server.getLevel(Level.OVERWORLD);
                if (level != null) currentTime = level.getGameTime();
                break;
            case DAY_TIME:
                ServerLevel overworld = server.getLevel(Level.OVERWORLD);
                if (overworld != null) currentTime = overworld.getDayTime() % 24000;
                break;
        }

        // Если startTime == endTime, считаем, что акция бессрочная (или отключена)
        if (startTime == endTime) return true;

        // Для зацикленного времени суток (например, с 18000 до 6000 - с вечера до утра)
        if (mode == TimeMode.DAY_TIME && startTime > endTime) {
            return currentTime >= startTime || currentTime <= endTime;
        }

        // Стандартная проверка интервала
        return currentTime >= startTime && currentTime <= endTime;
    }

    @Override
    public IComponentType<?> getType() {
        return TYPE;
    }

    private static class Type implements IComponentType<PromoTimeComponent> {

        private static final ResourceLocation ID = ResourceLocation.tryBuild("sdm", "promo_time");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public JsonObject serialize(PromoTimeComponent component) {
            JsonObject json = new JsonObject();
            json.addProperty("mode", component.mode.name());
            json.addProperty("start_time", component.startTime);
            json.addProperty("end_time", component.endTime);
            return json;
        }

        @Override
        public PromoTimeComponent deserialize(JsonObject json) {
            if (!json.has("mode")) {
                throw new JsonParseException("[PromoTimeComponent] Missing required parameter: 'mode'");
            }
            if (!json.has("start_time")) {
                throw new JsonParseException("[PromoTimeComponent] Missing required parameter: 'start_time'");
            }
            if (!json.has("end_time")) {
                throw new JsonParseException("[PromoTimeComponent] Missing required parameter: 'end_time'");
            }

            TimeMode mode;
            String modeStr = json.get("mode").getAsString();
            try {
                mode = TimeMode.valueOf(modeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new JsonParseException("[PromoTimeComponent] Invalid 'mode': '" + modeStr +
                        "'. Expected one of: " + Arrays.toString(TimeMode.values()));
            }

            try {
                long startTime = json.get("start_time").getAsLong();
                long endTime = json.get("end_time").getAsLong();
                return new PromoTimeComponent(mode, startTime, endTime);
            } catch (NumberFormatException | UnsupportedOperationException e) {
                throw new JsonParseException("[PromoTimeComponent] Parameters 'start_time' and 'end_time' must be valid numbers!");
            }
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, PromoTimeComponent component) {
            buf.writeEnum(component.mode);
            buf.writeLong(component.startTime);
            buf.writeLong(component.endTime);
        }

        @Override
        public PromoTimeComponent fromNetwork(FriendlyByteBuf buf) {
            final TimeMode mode = buf.readEnum(TimeMode.class);
            final long startTime = buf.readLong();
            final long endTime = buf.readLong();
            return new PromoTimeComponent(mode, startTime, endTime);
        }

        @Override
        public PromoTimeComponent createDefault() {
            return new PromoTimeComponent();
        }

        @Override
        public PromoTimeComponent createFromBuilder(Object... args) {
            if(args.length != 3 && args.length != 4)
                throw new IllegalArgumentException("PromoTimeComponent.createFromBuilder() takes 3 or 4 arguments (String, long, long, (Optional) String)");

            final var component = new PromoTimeComponent(TimeMode.valueOf((String) args[0]), (long) args[1], (long) args[2]);
            if(args.length == 4)
                component.setPromoId((String) args[3]);

            return component;
        }
    }
}
