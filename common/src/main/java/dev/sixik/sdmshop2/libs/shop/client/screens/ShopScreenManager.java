package dev.sixik.sdmshop2.libs.shop.client.screens;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.ShopScreen;
import dev.sixik.sdmshop2.utils.ShopUtils;

public class ShopScreenManager {

    public static final ShopScreenManager INSTANCE = new ShopScreenManager();

    protected WidgetGroup createGui() {
        return new ShopScreen();
    }

    public void openGui() {
        ShopUtils.openWidget(createGui());
    }
}
