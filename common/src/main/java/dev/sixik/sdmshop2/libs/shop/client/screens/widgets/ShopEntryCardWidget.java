package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;

public class ShopEntryCardWidget extends ShopWidgetGroup {

    public static final ColorRectAndBorderTexture TEXTURE = new ColorRectAndBorderTexture(
            ShopColors.BG_CARD, ShopColors.BORDER, 1
    ).setRadius(4);

    public static final ColorRectAndBorderTexture TEXTURE_OVER = new ColorRectAndBorderTexture(
            ShopColors.BG_CARD, ShopColors.BG_HOVER, 1
    ).setRadius(4);

    public ShopEntryCardWidget() {
        super();

        setBackground(TEXTURE);
        setHoverTexture(TEXTURE_OVER);
    }

    @Override
    public void initWidget() {
        addWidgets();
    }

    @Override
    public void alightWidgets() {
        super.alightWidgets();
    }
}
