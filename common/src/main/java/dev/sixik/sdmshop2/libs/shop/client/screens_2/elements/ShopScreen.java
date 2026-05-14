package dev.sixik.sdmshop2.libs.shop.client.screens_2.elements;

import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.platform.Window;
import dev.sixik.sdmshop2.libs.shop.client.SDMShopClient;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.base.ShopWidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import dev.sixik.sdmshop2.libs.shop.components.misc.CatalogComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.client.Minecraft;

public class ShopScreen extends ShopWidgetGroup {

    @Getter
    private final Minecraft minecraft;
    @Getter
    private final Window window;

    @Getter
    private ShopTabsPanel tabsPanel;

    @Getter
    private ObjectArrayList<CatalogComponent> catalogComponents;

    public ShopScreen() {
        this.minecraft = Minecraft.getInstance();
        this.window = minecraft.getWindow();
    }

    @Override
    public void initWidget() {
        setBackground(new ColorRectAndBorderTexture());

        this.catalogComponents = SDMShopClient.Shop.getCategories().getCatalogsComponents();

        alightWidget();
        addWidget(tabsPanel = new ShopTabsPanel(this));
        customInitWidget();
    }

    @Override
    public void alightWidget() {
        setSize(window.getGuiScaledWidth(), window.getGuiScaledHeight());
        super.alightWidget();
    }

    protected void alightWidgets() {
       /*
            cur_* = Current N
            ts_*  = TabsWidget
        */

        final Size cur_size = getSize();
        final int cur_w = cur_size.width;
        final int cur_h = cur_size.height;


        final int tsw_w = cur_w / 4;
        final int tsw_h_offset = (cur_h / 4);
        final int tsw_h = cur_h - tsw_h_offset * 2;
        tabsPanel.setSize(tsw_w, tsw_h);

        final int tsw_x = 0;
        final int tsw_y = tsw_h_offset;
        tabsPanel.setSelfPosition(tsw_x, tsw_y);
    }

    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        alightWidget();
    }
}
