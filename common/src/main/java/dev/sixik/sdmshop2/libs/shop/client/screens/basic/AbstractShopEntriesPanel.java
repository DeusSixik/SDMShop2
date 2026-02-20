package dev.sixik.sdmshop2.libs.shop.client.screens.basic;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public class AbstractShopEntriesPanel extends WidgetGroup {

    public final AbstractShopScree shopScreen;

    public AbstractShopEntriesPanel(AbstractShopScree shopScreen) {
        this.shopScreen = shopScreen;
        setupDefaultSettings();
    }

    protected void setupDefaultSettings() { }
}
