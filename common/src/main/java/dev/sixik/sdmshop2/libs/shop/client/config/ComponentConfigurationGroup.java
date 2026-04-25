package dev.sixik.sdmshop2.libs.shop.client.config;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.CollapsedGroupWidget;
import dev.sixik.sdmshop2.libs.shop.components.promo.effects.DiscountComponent;
import dev.sixik.sdmshop2.libs.shop.scripting.ScriptConditionComponent;
import dev.sixik.sdmshop2.libs.shop.scripting.ScriptRewardComponent;
import dev.sixik.sdmshop2.tests.TestComponent;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class ComponentConfigurationGroup extends WidgetGroup {

    public static final ShopOffer offer;

    static {
        offer = ShopOffer.create(UUID.randomUUID(), false);
        offer.addComponent(new DiscountComponent());
        offer.addComponent(new ScriptRewardComponent());
        offer.addComponent(new ScriptConditionComponent());
    }

    @Override
    public void initWidget() {
        setSize(400, 400);

        DraggableScrollableWidgetGroup scrollPanel = new DraggableScrollableWidgetGroup(10, 10, 200, 250);
        scrollPanel.setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.VERTICAL);
        scrollPanel.setLayout(Layout.NONE);
        scrollPanel.setYScrollBarWidth(6);
        scrollPanel.setYBarStyle(
                null,
                ColorPattern.WHITE.rectTexture().setRadius(2)
        );

        WidgetGroup contentWrapper = new WidgetGroup(0, 0, 200, 0);
        contentWrapper.setLayout(Layout.VERTICAL_LEFT);
        contentWrapper.setLayoutPadding(2);
        contentWrapper.setDynamicSized(true);



        ComponentConfigWidgetConstructor.createShopOfferWidget(contentWrapper, offer, 200);
        scrollPanel.addWidget(contentWrapper);
        this.addWidget(scrollPanel);

        scrollPanel.setClientSideWidget();
        super.initWidget();
    }
}
