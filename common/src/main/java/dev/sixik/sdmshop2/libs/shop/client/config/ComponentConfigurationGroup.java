package dev.sixik.sdmshop2.libs.shop.client.config;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.CollapsedGroupWidget;
import net.minecraft.network.chat.Component;

public class ComponentConfigurationGroup extends WidgetGroup {

    @Override
    public void initWidget() {
        DraggableScrollableWidgetGroup scrollPanel = new DraggableScrollableWidgetGroup(10, 10, 200, 250);
        scrollPanel.setScrollWheelDirection(DraggableScrollableWidgetGroup.ScrollWheelDirection.VERTICAL);
        scrollPanel.setLayout(Layout.NONE);

        WidgetGroup contentWrapper = new WidgetGroup(0, 0, 190, 0);
        contentWrapper.setLayout(Layout.VERTICAL_LEFT);
        contentWrapper.setLayoutPadding(2);
        contentWrapper.setDynamicSized(true);


        CollapsedGroupWidget group1 = new CollapsedGroupWidget(Component.literal("Группа 1 (Ресурсы)"), 190);
        group1.addWidget(new ComponentConfigurationWidget());

        CollapsedGroupWidget group3 = new CollapsedGroupWidget(Component.literal("Группа 1.1"), 190);
        group3.addWidget(new ComponentConfigurationWidget());
        group1.addWidget(group3);

        CollapsedGroupWidget group2 = new CollapsedGroupWidget(Component.literal("Группа 2 (Настройки)"), 190);
        group2.addWidget(new ComponentConfigurationWidget());

        contentWrapper.addWidget(group1);
        contentWrapper.addWidget(group2);

        scrollPanel.addWidget(contentWrapper);
        this.addWidget(scrollPanel);

        super.initWidget();
    }
}
