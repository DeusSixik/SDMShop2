package dev.sixik.sdmshop2.libs.shop.client.screens.basic;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.blaze3d.platform.Window;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import lombok.Getter;
import net.minecraft.client.Minecraft;

public abstract class AbstractShopScree extends WidgetGroup {

    @Getter
    private final ShopInstance selectedShop;

    public AbstractShopScree(ShopInstance selectedShop) {
        this.selectedShop = selectedShop;
        final Minecraft minecraft = Minecraft.getInstance();
        setupDefaultSettings(minecraft, minecraft.getWindow());
    }

    @Override
    public final void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        onScreenSizeChange(screenWidth, screenHeight);
        super.onScreenSizeUpdate(screenWidth, screenHeight);
    }

    protected void setupDefaultSettings(Minecraft minecraft, Window window) {
        setSize(
                window.getGuiScaledWidth() * 4/5,
                window.getGuiScaledHeight() * 4/5
        );
        setSelfPosition(Position.ORIGIN);
    }

    protected abstract void onScreenSizeChange(int screenWidth, int screenHeight);
}
