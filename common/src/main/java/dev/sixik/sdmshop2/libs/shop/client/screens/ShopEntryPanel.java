package dev.sixik.sdmshop2.libs.shop.client.screens;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import dev.sixik.sdmshop2.libs.shop.base.ShopEntry;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.ShopEntryCardWidget;
import dev.sixik.sdmshop2.libs.shop.components.CategoryComponent;
import dev.sixik.sdmshop2.libs.shop.components.ShopCategoriesContainerComponent;
import dev.sixik.sdmshop2.libs.shop.components.ShopEntriesContainerComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.UUID;

public class ShopEntryPanel extends DraggableScrollableWidgetGroup {

    // Размеры карточки товара и отступы (можешь менять под свой дизайн)
    private static final int CARD_WIDTH = 120;
    private static final int CARD_HEIGHT = 50;
    private static final int PADDING = 10;

    public ShopEntryPanel(int x, int y, int width, int height) {
        super(x, y, width, height);
        // Отключаем встроенный Layout, мы будем управлять позициями сами
        setLayout(Layout.NONE);
    }

    public void addEntries(ShopInstance instance) {
        // 1. Сначала очищаем старые виджеты, если панель обновляется
        clearAllWidgets();

        // 2. Узнаем доступную ширину панели, чтобы понять, сколько колонок влезет
        // Если у нас ScrollGroup, нужно вычесть ширину скроллбара (обычно около 10-14 пикселей)
        int availableWidth = this.getSizeWidth() - 14;

        // 3. Считаем количество колонок
        // Формула учитывает ширину карточки и отступы между ними
        int columns = Math.max(1, (availableWidth + PADDING) / (CARD_WIDTH + PADDING));

        int index = 0;

        // 4. Добавляем карточки и расставляем их по сетке
        for (Map.Entry<UUID, ShopEntry> mapEntry : instance.getEntries().getEntryMap().entrySet()) {
            ShopEntry entry = mapEntry.getValue();

            // Создаем виджет твоей карточки (передаем размеры, если они не заданы внутри)
            ShopEntryCardWidget cardWidget = new ShopEntryCardWidget();
            cardWidget.setSize(CARD_WIDTH, CARD_HEIGHT);

            // --- МАТЕМАТИКА СЕТКИ ---
            // Вычисляем текущую строку и колонку для этого индекса
            int col = index % columns; // Остаток от деления (0, 1, 2...)
            int row = index / columns; // Целочисленное деление (0, 0, 0, 1, 1, 1...)

            // Вычисляем координаты X и Y
            int x = col * (CARD_WIDTH + PADDING);
            int y = row * (CARD_HEIGHT + PADDING);

            // Устанавливаем позицию виджета относительно этой панели
            cardWidget.setSelfPosition(x, y);

            // Добавляем виджет в группу
            addWidget(cardWidget);

            index++;
        }
    }

    public void recalculateGrid() {
        int index = 0;

        int availableWidth = this.getSizeWidth() - 14;
        int columns = Math.max(1, (availableWidth + PADDING) / (CARD_WIDTH + PADDING));

        for (Widget containedWidget : getContainedWidgets(true)) {
            int col = index % columns; // Остаток от деления (0, 1, 2...)
            int row = index / columns; // Целочисленное деление (0, 0, 0, 1, 1, 1...)

            // Вычисляем координаты X и Y
            int x = col * (CARD_WIDTH + PADDING);
            int y = row * (CARD_HEIGHT + PADDING);

            // Устанавливаем позицию виджета относительно этой панели
            containedWidget.setSelfPosition(x, y);

            index++;
        }
    }
}