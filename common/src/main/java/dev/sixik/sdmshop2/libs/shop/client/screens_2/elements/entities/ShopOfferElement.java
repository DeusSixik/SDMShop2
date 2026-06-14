package dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.entities;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.sixik.sdmshop2.libs.platform.render.RenderHelpUtils;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.SDMTextLabel;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.SDMZoneTextLabel;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.ShopEmptyWidget;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.ShopUiElement;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import dev.sixik.sdmshop2.libs.shop.components.misc.NameComponent;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

public class ShopOfferElement extends WidgetGroup implements ShopUiElement {

    @Getter
    @Nullable
    private final ShopOffer shopEntity;

    // Используем "резиновую зону" для названия
    @Nullable
    private SDMZoneTextLabel nameLabel;

    private ShopEmptyWidget iconWidget;
    private ShopEmptyWidget backgroundTitleWidget;

    // Элементы для бейджей (скидки, лимиты)
    private ShopEmptyWidget badgeBackground;
    private SDMTextLabel badgeText;

    public ShopOfferElement(@Nullable ShopOffer shopEntity) {
        this.shopEntity = shopEntity;
        setBackground(new ColorRectAndBorderTexture());

        // 1. Фон хэдера
        addWidget(backgroundTitleWidget = new ShopEmptyWidget());
        backgroundTitleWidget.setBackground(new ColorRectTexture(0xFF1E1E2A));

        // 2. Иконка
        addWidget(iconWidget = new ShopEmptyWidget());
        iconWidget.setBackground(new ItemStackTexture(new ItemStack(Items.DIAMOND, 4)));

        // 3. Бейдж (Например: Лимит / Скидка). Делаем заглушку красного цвета.
        addWidget(badgeBackground = new ShopEmptyWidget());
        badgeBackground.setBackground(new ColorRectTexture(0xFFAA0000).setRadius(2)); // Темно-красный


        addWidget(badgeText = new SDMTextLabel(Component.literal("Лим. 1")));

        // 4. Название (Zone Label)
        NameComponent nameComponent = shopEntity.getComponent(NameComponent.class).orElse(null);
        if(nameComponent != null) {
            // Инициализируем нулями, правильный размер задаст alightWidget
            nameLabel = new SDMZoneTextLabel(0, 0, 10, 10, Component.translatable(nameComponent.getName()));
            addWidget(nameLabel);
        }
    }

    @Override
    public void alightWidget() {
        int space_x = 4;
        int space_y = 4;

        // --- 1. ВЫРАВНИВАНИЕ ФОНА ХЭДЕРА ---
        int headerWidth = this.getSizeWidth() - space_x * 2;
        int headerHeight = this.getSizeHeight() / 4; // Высота хэдера - четверть плашки

        backgroundTitleWidget.setSelfPosition(space_x, space_y);
        backgroundTitleWidget.setSize(headerWidth, headerHeight);

        // --- 2. ВЫРАВНИВАНИЕ ИКОНКИ (Якорь слева) ---
        int iconPadding = 2;
        int iconSize = headerHeight - iconPadding * 2; // Квадратная иконка по высоте хэдера

        iconWidget.setSelfPosition(space_x + iconPadding, space_y + iconPadding);
        iconWidget.setSize(iconSize, iconSize);

        int badgeWidth = 26;
        int badgeHeight = 10;
        int badgeX = (space_x + headerWidth) - badgeWidth - iconPadding;
        int badgeY = space_y + iconPadding;

        badgeBackground.setSelfPosition(badgeX, badgeY);
        badgeBackground.setSize(badgeWidth, badgeHeight);

        badgeText.setSize(badgeWidth, badgeHeight);
        badgeText.setSelfPosition(badgeX, badgeY);

        badgeText.setScale(1.0f);
        badgeText.setAutoScale(true);
        badgeText.setPadding(2);

        if(nameLabel != null) {
            int titleX = iconWidget.getSelfPositionX() + iconSize + 4;
            int titleY = space_y + iconPadding;
            int titleWidth = badgeX - titleX - 4;
            int titleHeight = headerHeight - iconPadding * 2;

            nameLabel.setSelfPosition(titleX, titleY);
            nameLabel.setSize(titleWidth, titleHeight);
        }
    }
}
