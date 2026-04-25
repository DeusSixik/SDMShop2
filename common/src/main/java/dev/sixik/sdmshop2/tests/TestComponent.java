package dev.sixik.sdmshop2.tests;

import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.annotation.ComponentConfig;
import dev.sixik.sdmshop2.libs.shop.components.limiter.LimiterComponent;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class TestComponent extends ShopComponent {


    @ComponentConfig
    private UUID rootId;

    @Getter
    @Setter
    @ComponentConfig(translationKey = "shop.component.limiter.limiter.limiter_type")
    private LimiterComponent.LimiterType limiterType = LimiterComponent.LimiterType.Player;

    @Getter
    @Setter
    @ComponentConfig(translationKey = "shop.component.limiter.limiter.count")
    private int count;

    @Getter
    @Setter
    @ComponentConfig(translationKey = "shop.component.limiter.limiter.reset_interval_ms")
    private long resetIntervalMs;

    @ComponentConfig
    private boolean test_boolean = true;

    @ComponentConfig
    private Item item = Items.AIR;

    @ComponentConfig
    private ItemStack itemStack = ItemStack.EMPTY;

    @ComponentConfig
    private Block block = Blocks.AIR;

    @ComponentConfig
    private BlockState blockState = Blocks.AIR.defaultBlockState();

    @ComponentConfig
    private String test_string = "test";

    @ComponentConfig
    private ResourceLocation test_resource_location = ResourceLocation.tryBuild("sdm", "test");

    @Override
    public IComponentType<?> getType() {
        throw new UnsupportedOperationException("Component type not implemented");
    }
}
