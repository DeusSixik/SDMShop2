package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public class ShopWidgetGroup extends WidgetGroup {

    public ShopWidgetGroup() {
        addWidgets();
        alightWidgets();
    }

    public void addWidgets() { }

    public void alightWidgets() { }

    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        alightWidgets();
        super.onScreenSizeUpdate(screenWidth, screenHeight);
    }
}
