package dev.sixik.sdmshop2.libs.shop.client.screens.test;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;

public class CreateOfferPanel extends WidgetGroup {

    protected int panelSize;

   public CreateOfferPanel(){

   }

    public void createSettingPanel(){
        WidgetGroup editingPanel = new WidgetGroup(0,0,panelSize,panelSize);
        editingPanel.setBackground(new ColorRectTexture(ShopColors.BG_PANEL).setRadius(3));
        editingPanel.setAlign(Align.CENTER);
        addWidget(editingPanel);
    }

    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {

        setSize(screenWidth, screenHeight);
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        int a = Math.min(screenHeight,screenWidth);
        panelSize = (int) (a*0.7);
        clearAllWidgets();
        createSettingPanel();

    }
}
