package dev.sixik.sdmshop2.libs.shop.components.limiter;

import dev.sixik.sdmshop2.libs.shop.components.api.ConditionComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.entity.player.Player;

public class LimiterComponent extends ConditionComponent {

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
    public boolean isChecked(Player player) {
        return false;
    }

    @Override
    public IComponentType<?> getType() {
        return null;
    }

    public enum LimiterType {
        World,
        Player
    }
}
