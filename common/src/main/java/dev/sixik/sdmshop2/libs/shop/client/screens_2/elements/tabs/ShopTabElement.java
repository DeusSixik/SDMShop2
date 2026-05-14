package dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.tabs;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.platform.InputConstants;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.ShopUiElement;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.SDMTextLabel;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import dev.sixik.sdmshop2.libs.shop.components.misc.CatalogComponent;
import dev.sixik.sdmshop2.utils.ShopUtils;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class ShopTabElement extends WidgetGroup implements ShopUiElement {

    public static final Component DEFAULT_TITLE = Component.literal("No Title");

    @Getter
    private final @Nullable CatalogComponent component;

    @Getter
    private final SDMTextLabel textLabel;

    public ShopTabElement(
            @Nullable CatalogComponent component
    ) {
        this(0, 0, 100, 20, component);
    }

    private ShopTabElement(
            int x, int y, int width, int height,
            @Nullable CatalogComponent component
    ) {
        super(x, y, width, height);
        this.component = component;
        this.textLabel = new SDMTextLabel(
                component != null
                        ? ShopUtils.getTranslation(component.getId())
                        : DEFAULT_TITLE
        );
        this.textLabel.color = ShopColors.TEXT_MAIN;

        setBackground(new ColorRectAndBorderTexture(0x332A2A36, 0x553D3D4E, 1).setRadius(5));
        setHoverTexture(new ColorRectAndBorderTexture(0x555C6BC0, 0xAA7986CB, 1).setRadius(5));
        setDrawBackgroundWhenHover(false);
        addWidget(textLabel);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean isClick = super.mouseClicked(mouseX, mouseY, button);
        if(isClick && button == InputConstants.MOUSE_BUTTON_LEFT) {


            return true;
        }

        return false;
    }

    @Override
    public void alightWidget() {
        int labelX = 8;
        int labelY = Math.max(0, (getSizeHeight() - Minecraft.getInstance().font.lineHeight) / 2);
        textLabel.setSelfPosition(labelX, labelY);
    }
}
