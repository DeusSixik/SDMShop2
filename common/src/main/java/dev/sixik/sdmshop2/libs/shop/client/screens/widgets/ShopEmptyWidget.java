package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

public class ShopEmptyWidget extends Widget {

    public ShopEmptyWidget() {
        this(0, 0, 32, 32);
    }

    public ShopEmptyWidget(Position selfPosition, Size size) {
        super(selfPosition, size);
    }

    public ShopEmptyWidget(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
}
