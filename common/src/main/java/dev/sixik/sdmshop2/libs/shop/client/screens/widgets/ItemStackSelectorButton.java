package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.screens.test.ItemStackSelector;
import net.minecraft.world.item.ItemStack;


public class ItemStackSelectorButton extends WidgetGroup {

    public ItemStack item = null;

   public ItemStackSelectorButton(WidgetGroup panel){

       Widget icon = new WidgetGroup(0,0,getSizeWidth(),getSizeHeight());
       icon.setBackground(new ColorRectTexture(ShopColors.BG_ORANGE));
       addWidget(icon);
       ButtonWidget selectorButton = new ButtonWidget(0,0,getSizeWidth(),getSizeHeight(),clickData -> {
           WidgetGroup selector = new ItemStackSelector(item, selectedStack -> {
               // Коллбэк сработает, когда игрок кликнет по предмету в сетке
               icon.setBackground(new GuiTextureGroup(new ColorRectTexture(ShopColors.BG_PANEL),new ItemStackTexture(selectedStack)));
               item = selectedStack;
               // Тут ты обновляешь данные своего товара
           });
           selector.initWidget();
           parent.addWidget(selector);
       });
       addWidget(selectorButton);
   }

    @Override
    public void initWidget() {
       System.out.println("Init widgets ItemStackSelectorButton");
        super.initWidget();
    }
}
