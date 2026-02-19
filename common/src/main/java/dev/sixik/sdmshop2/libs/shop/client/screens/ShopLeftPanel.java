package dev.sixik.sdmshop2.libs.shop.client.screens;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.ShopEntryCardWidget;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;
import net.minecraft.client.Minecraft;

public class ShopLeftPanel extends WidgetGroup {

    public final ShopScreen shopScreen;

    public final ButtonWidget testButton;

    public ShopLeftPanel(ShopScreen shopScreen) {
        this.shopScreen = shopScreen;

        setSize(shopScreen.getSize().width / 6, shopScreen.getSize().height);
        setBackground(new ColorRectTexture(0xff1fafa0).setRadius(6));

        testButton = new ButtonWidget();
        testButton.setButtonTexture(ResourceBorderTexture.BUTTON_COMMON, new TextTexture("Test Button"));

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



        TextFieldWidget fieldWidget = new TextFieldWidget();
        fieldWidget.setBackground(ShopEntryCardWidget.TEXTURE);
        fieldWidget.setBordered(false);
        fieldWidget.setSize(100, 20);
        addWidget(fieldWidget);
    }

    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        final Size shopSize = shopScreen.getSize();
        setSize(shopSize.width / 6, shopSize.height);

        super.onScreenSizeUpdate(screenWidth, screenHeight);
    }
}
