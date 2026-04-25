package dev.sixik.sdmshop2.libs.shop.client.config;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.CollapsedGroupWidget;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.ExternTextFieldWidget;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.SDMTextLabel;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.tests.TestComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ComponentConfigurationWidget extends WidgetGroup {

    public static final ColorRectAndBorderTexture texture = new ColorRectAndBorderTexture();
    public static final ColorBorderTexture hoverTexture = new ColorBorderTexture(1, -1);

    @Getter
    @Nullable
    protected ShopComponent selectedComponent;

    @Setter
    protected Consumer<SwitchWidget> modifySwitchWidgetCallback = (widget) -> {
        widget.setTexture(new GuiTextureGroup(texture, new TextTexture("off")), new GuiTextureGroup(texture, new TextTexture("on")));
    };

    @Setter
    protected Consumer<ExternTextFieldWidget> modifyExternTextFieldWidgetCallback = (widget) -> {
        widget.setTextFieldHeight(20);
    };

    public ComponentConfigurationWidget() {
        super(0, 0, 180, 0);
        this.setDynamicSized(true);
        this.setLayout(Layout.NONE);
    }

    @Override
    public void initWidget() {
        var targetComponent = new TestComponent();

        ObjectArrayList<ComponentConfigAccess.CachedField> fieldsList =
                ComponentConfigAccess.getCachedFields(targetComponent.getClass());

        List<Widget[]> uiPairs = new ArrayList<>();

        for (int i = 0; i < fieldsList.size(); i++) {
            final var datum = fieldsList.get(i);
            Widget editorWidget = ComponentConfigWidgetConstructor.createWidget(targetComponent, datum);
            if (editorWidget == null) continue;
            if(editorWidget instanceof SwitchWidget switchWidget) {
                modifySwitchWidgetCallback.accept(switchWidget);
            } else if(editorWidget instanceof ExternTextFieldWidget textFieldWidget) {
                modifyExternTextFieldWidgetCallback.accept(textFieldWidget);
            }

            editorWidget.setHoverTexture(hoverTexture);
            editorWidget.setSizeHeight(20);

            SDMTextLabel textLabel = new SDMTextLabel(Component.translatable(datum.translationKey()));
            editorWidget.setBackground(texture);
            textLabel.setBackground(texture);

            uiPairs.add(new Widget[]{textLabel, editorWidget});
        }


        /*
            Самый верхний элемент интерфейса (index 0) будет добавлен последним,
            поэтому его DropDown перекроет все нижние виджеты и заберет клики.
         */
        for (int i = uiPairs.size() - 1; i >= 0; i--) {
            Widget[] pair = uiPairs.get(i);
            this.addWidget(pair[0]); // Label
            this.addWidget(pair[1]); // Editor
        }

        super.initWidget();

        final Font font = Minecraft.getInstance().font;
        int currentY = 10;

        /*
            Идем по нашему правильному списку
         */
        for (Widget[] pair : uiPairs) {
            SDMTextLabel label = (SDMTextLabel) pair[0];
            Widget editor = pair[1];

            label.setSelfPosition(10, currentY + 7);
            editor.setSelfPosition(14 + font.width(label.getText()), currentY);

            currentY += 25;
        }
    }

    public ComponentConfigurationWidget setSelectedComponent(@Nullable ShopComponent component) {
        this.selectedComponent = component;
        return this;
    }
}
