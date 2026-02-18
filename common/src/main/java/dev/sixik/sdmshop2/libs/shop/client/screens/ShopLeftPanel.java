package dev.sixik.sdmshop2.libs.shop.client.screens;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;
import net.minecraft.client.Minecraft;

public class ShopLeftPanel extends WidgetGroup {

    public final ShopScreen shopScreen;

    public final ButtonWidget testButton;

    public ShopLeftPanel(ShopScreen shopScreen) {
        this.shopScreen = shopScreen;

        setSize(shopScreen.getSize().width / 4, shopScreen.getSize().height);
        setBackground(new ColorRectTexture(0xff1fafa0).setRadius(6));

        testButton = new ButtonWidget();
        testButton.setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("Test Button"));


//        testButton.setSelfPosition(7, 40);

        var backgroundImage = ResourceBorderTexture.BUTTON_COMMON;
        var hoverImage = backgroundImage.copy().setColor(ColorPattern.CYAN.color);
        var textAbove = new TextTexture("Test Button Click!");
        testButton.setButtonTexture(backgroundImage, textAbove);
        testButton.setClickedTexture(hoverImage, textAbove);

        var k1 = Minecraft.getInstance().font.width(textAbove.text);

        testButton.setSizeWidth((int) (k1 + k1 * 0.2));

        Size buttonSize = testButton.getSize();

        var thisW = this.getSize().width;
        var thisH = this.getSize().height;

        testButton.setSelfPosition((thisW - buttonSize.width) / 2, thisH - buttonSize.getHeight() - 2);

        testButton.setOnPressCallback((s) -> EconomyTest.test());

        addWidget(testButton);
    }

    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        final Size shopSize = shopScreen.getSize();
        setSize(shopSize.width / 4, shopSize.height);

        super.onScreenSizeUpdate(screenWidth, screenHeight);
    }
}
