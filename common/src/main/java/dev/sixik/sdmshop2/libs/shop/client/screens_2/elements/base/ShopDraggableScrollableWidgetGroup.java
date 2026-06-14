package dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.base;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.ShopUiElement;

public class ShopDraggableScrollableWidgetGroup extends DraggableScrollableWidgetGroup implements ShopUiElement {

    @Override
    public void initWidget() {
        alightWidget();
        customInitWidget();
        alightWidgets();
    }

    protected final void customInitWidget() {
        initialized = true;
        isClientSideWidget = true;
        for (int i = 0; i < widgets.size(); i++) {
            Widget widget = widgets.get(i);
            if (widget.getGui() != gui) {
                widget.setGui(gui);
            }
            widget.setClientSideWidget();
            widget.initWidget();
        }

        alightWidgets();
    }

    @Override
    public void alightWidget() {
        invokeAlightWidgets();
    }

    protected void alightWidgets() {

    }

    public final void invokeAlightWidgets() {
        if(!initialized) return;

        for (int i = 0; i < widgets.size(); i++) {
            if(widgets.get(i) instanceof ShopUiElement element)
                element.alightWidget();
        }
    }
}
