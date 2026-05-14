package dev.sixik.sdmshop2.libs.shop.client.screens_2.elements;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.base.ShopDraggableScrollableWidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.tabs.ShopTabElement;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import dev.sixik.sdmshop2.libs.shop.components.misc.CatalogComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Locale;

public class ShopTabsPanel extends ShopDraggableScrollableWidgetGroup implements ShopUiElement {

    @Getter
    @Setter
    public int widgetSpace = 4;

    private static final int HORIZONTAL_PADDING = 6;
    private static final int VERTICAL_PADDING = 6;
    private static final int TAB_HEIGHT = 24;
    private static final int SCROLLBAR_WIDTH = 3;

    @Getter
    private final ShopScreen shopScreen;

    public ShopTabsPanel(@NotNull ShopScreen screen) {
        this.shopScreen = screen;
    }

    @Override
    public void initWidget() {
        setBackground(new ColorRectAndBorderTexture(0x66000000, ShopColors.BORDER, 1).setRadius(6));
        setYScrollBarWidth(SCROLLBAR_WIDTH).setYBarStyle(null, new ColorRectTexture(-1));
        super.initWidget();
    }

    @Override
    public void alightWidget() {
        rebuildCatalogs();

        if (!initialized) return;

        for (int i = 0; i < widgets.size(); i++) {
            if (widgets.get(i) instanceof ShopUiElement element)
                element.alightWidget();
        }
    }

    protected void alightWidgets() {
        alightWidget();
    }

    public void rebuildCatalogs() {
        clearAllWidgets();
        ObjectArrayList<CatalogComponent> catalogList = shopScreen.getCatalogComponents();
        if (catalogList == null || catalogList.isEmpty()) return;

        catalogList.sort(Comparator.comparing(
                component -> component.getId() == null ? "" : component.getId().toLowerCase(Locale.ROOT)
        ));

        Object[] catalogArray = catalogList.elements();
        int width = Math.max(1, getSizeWidth() - HORIZONTAL_PADDING * 2 - SCROLLBAR_WIDTH);
        for (int i = 0; i < catalogList.size(); i++) {
            CatalogComponent component = (CatalogComponent) catalogArray[i];
            ShopTabElement element = new ShopTabElement(component);
            element.setSize(width, TAB_HEIGHT);
            element.alightWidget();
            addWidget(element);
            element.setSelfPosition(
                    HORIZONTAL_PADDING,
                    VERTICAL_PADDING + (TAB_HEIGHT + widgetSpace) * i
            );
        }
    }
}
