package dev.sixik.sdmshop2.libs.shop.client.screens.basic;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public abstract class AbstractTabsShopPanel extends WidgetGroup {

    public final AbstractShopScree shopScreen;

    public AbstractTabsShopPanel(AbstractShopScree shopScreen) {
        this.shopScreen = shopScreen;
        setupDefaultSettings();
    }

    protected void setupDefaultSettings() { }
}
