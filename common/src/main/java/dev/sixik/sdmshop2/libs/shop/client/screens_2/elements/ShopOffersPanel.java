package dev.sixik.sdmshop2.libs.shop.client.screens_2.elements;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.base.ShopDraggableScrollableWidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.screens_2.elements.entities.ShopOfferElement;
import dev.sixik.sdmshop2.libs.shop.components.misc.ShopOffersContainerComponent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public class ShopOffersPanel extends ShopDraggableScrollableWidgetGroup implements ShopUiElement {

    private static final int ITEM_HEIGHT = 90; // Высота карточки
    private static final int SPACING = 5;
    private static final int SCROLLBAR_WIDTH = 3;

    @Getter
    protected final @NotNull ShopScreen shopScreen;

    public ShopOffersPanel(@NotNull ShopScreen shopScreen) {
        this.shopScreen = shopScreen;
        setBackground(new ColorRectTexture(0xff1fafa0).setRadius(6));
    }

    @Override
    public void initWidget() {
        setYScrollBarWidth(SCROLLBAR_WIDTH).setYBarStyle(null, new ColorRectTexture(-1));
        super.initWidget();
    }

    @Override
    public void alightWidget() {
        rebuildOffers();

        if (!initialized) return;

        for (int i = 0; i < widgets.size(); i++) {
            if (widgets.get(i) instanceof ShopUiElement element)
                element.alightWidget();
        }
    }

    @Override
    protected void alightWidgets() {
        alightWidget();
    }

    public void rebuildOffers() {
        clearAllWidgets();

        final ShopOffersContainerComponent entriesContainer = shopScreen.getEntriesContainer();

        final Size size = this.getSize();

        for (ShopOffer value : entriesContainer.getEntryMap().values()) {
            ShopOfferElement offerElement = new ShopOfferElement(value);

            // Обязательно задаем размер элементу перед тем, как его выравнивать
            offerElement.setSize(size.width / 2 - 20, ITEM_HEIGHT);

            // Добавляем виджет в эту панель (название метода может отличаться, например addWidget)
            this.addWidget(offerElement);
        }

        alightOffers();
    }

    public void alightOffers() {
        if (!initialized || widgets.isEmpty()) return;
        final Size widgetSize = widgets.get(0).getSize();

        // 1. Доступная ширина для карточек (учитываем скроллбар)
        int panelWidth = this.getSizeWidth() - SCROLLBAR_WIDTH;

        // 2. Вычисляем максимально возможное количество колонок
        int columns = Math.max(1, (panelWidth - SPACING) / (widgetSize.width + SPACING));

        // Центрируем элементы корректно, даже если их меньше, чем ширина строки
        columns = Math.min(columns, widgets.size());



        // 3. Вычисляем общую ширину всей получившейся сетки
        int totalGridWidth = (columns * widgetSize.width) + ((columns - 1) * SPACING);

        // 4. Находим динамический левый отступ для центрирования
        int startX = Math.max(SPACING, (this.getSizeWidth() - totalGridWidth) / 2);

        // 5. Располагаем каждый виджет по центрированной сетке
        for (int i = 0; i < widgets.size(); i++) {
            var widget = widgets.get(i);

            int row = i / columns;
            int col = i % columns;

            // X отсчитывается от центрированного отступа startX
            int x = startX + col * (widgetSize.width + SPACING);
            // Y остается неизменным (сверху вниз)
            int y = SPACING + row * (ITEM_HEIGHT + SPACING);

            widget.setSelfPosition(x, y);

            if (widget instanceof ShopUiElement element) {
                element.alightWidget();
            }
        }
    }
}
