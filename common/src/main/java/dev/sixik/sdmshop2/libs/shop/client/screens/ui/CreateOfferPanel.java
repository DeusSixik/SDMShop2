package dev.sixik.sdmshop2.libs.shop.client.screens.ui;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.platform.Window;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.ItemStackSelectorButton;
import net.minecraft.client.Minecraft;

public class CreateOfferPanel extends WidgetGroup {

    protected int panelSize;
    protected WidgetGroup editingPanel;

    protected final Window window;

   public CreateOfferPanel(){
//       createSettingPanel();

       this.window = Minecraft.getInstance().getWindow();
   }

    public void createSettingPanel(){
        final int w = window.getGuiScaledWidth();
        final int h = window.getGuiScaledHeight();
        final int factor = Math.min(w,h);
        panelSize = (int) (factor * 0.7);
        editingPanel = new WidgetGroup(
                (w - panelSize) / 2,
                (h - panelSize) / 2,
                panelSize,
                panelSize
        );
        editingPanel.setBackground(new ColorRectTexture(ShopColors.BG_PANEL).setRadius(3));

        addWidget(editingPanel);

        WidgetGroup test = new ItemStackSelectorButton(editingPanel);
        test.setSelfPosition(2,2);
        test.setSize(28,28);
        editingPanel.addWidget(test);
    }

    @Override
    public void initWidget() {
        System.out.println("Init widgets CreateOfferPanel");
        createSettingPanel();
        super.initWidget();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        setSize(screenWidth, screenHeight);
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        final int factor = Math.min(screenHeight,screenWidth);
        panelSize = (int) (factor*0.7);

        editingPanel.setSize(panelSize,panelSize);
        editingPanel.setSelfPosition(
                (screenWidth - panelSize) / 2,
                (screenHeight - panelSize) / 2
        );
    }
}
