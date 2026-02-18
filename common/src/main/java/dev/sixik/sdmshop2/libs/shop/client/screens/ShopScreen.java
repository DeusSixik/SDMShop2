package dev.sixik.sdmshop2.libs.shop.client.screens;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

public class ShopScreen extends WidgetGroup {

    public final ShopLeftPanel leftPanel;

    public ShopScreen() {
        setBackground(new ColorRectTexture().setRadius(6));

        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = minecraft.getWindow();

        final int w = window.getGuiScaledWidth();
        final int h = window.getGuiScaledHeight();

        setSize(w - w / 6, h - h / 6);

        addWidget(leftPanel = new ShopLeftPanel(this));
    }

    @Override
    public void onScreenSizeUpdate(int w, int h) {
        setSize(w - w / 6, h - h / 6);

        setSelfPosition(Position.ORIGIN);

        final ModularUI gui = getGui();
        gui.setSize(getSize().width, getSize().height);
        super.onScreenSizeUpdate(w, h);
    }
}
