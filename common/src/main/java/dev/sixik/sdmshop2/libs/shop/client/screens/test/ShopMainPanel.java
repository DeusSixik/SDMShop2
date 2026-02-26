package dev.sixik.sdmshop2.libs.shop.client.screens.test;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.screens.ShopEntryPanel;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ShopMainPanel extends WidgetGroup {

    private final int sidebarWidth = 140;
    private final int topBarHeight = 32;

    private WidgetGroup sidebar;
    private ShopEntryPanel catalogGrid; // Тот самый скролл-виджет, который мы писали ранее

    public ShopMainPanel() {
        final Minecraft minecraft = Minecraft.getInstance();

        setBackground(new ColorRectTexture(ShopColors.BG_BASE));
    }

    public void openCustomModal() {
        // 1. Создаем диалог. isClient = true
        DialogWidget dialog = new DialogWidget(this, true);

        // 2. Делаем остальные виджеты на фоне невидимыми/неактивными (эффект модальности)
        dialog.setParentInVisible();

        // 3. Создаем контейнер с красивой рамкой и фоном (ширина 250, высота 150)
        WidgetGroup container = DialogWidget.createContainer(dialog, 250, 150, "Редактирование товара");

        // 4. Добавляем в контейнер любые свои виджеты (кнопки, текстовые поля, иконки)
        container.addWidget(new LabelWidget(10, 20, "Настройте параметры:").setTextColor(ShopColors.TEXT_MAIN));

        // 5. Кнопка закрытия
        DialogWidget.createButton(container, 10, 120, 100, 15, "Закрыть", dialog::close);
    }

    private void buildSidebar() {
        sidebar = new WidgetGroup(0, 0, sidebarWidth, getSizeHeight());
        // Фон панели с правой рамкой
        sidebar.setBackground(new ColorRectAndBorderTexture(ShopColors.BG_PANEL, ShopColors.BORDER, 0)
                .setRightRadius(0)); // Радиус только если нужен, иначе просто прямая заливка

        // Заголовок
        LabelWidget title = new LabelWidget(10, 10, "Магазин").setTextColor(ShopColors.TEXT_MAIN);
        // В LDLib LabelWidget сам считает свой размер, но можно увеличить масштаб через матрицу или использовать TextTextureWidget
        sidebar.addWidget(title);

        WidgetGroup categoryGroup = new WidgetGroup(5, 33, sidebarWidth - 10, 25);

// 2. Создаем саму кнопку внутри группы (размер равен группе, позиция 0,0)
        ButtonWidget categoryBtn = new ButtonWidget(0, 0, categoryGroup.getSizeWidth(), categoryGroup.getSizeHeight(), cd -> {
            System.out.println("Выбрана категория: Оружие");
        });

        categoryBtn.setOnPressCallback((s) -> {
            openCustomModal();
        });

// 3. Добавляем в группу СНАЧАЛА кнопку (она будет фоном и ловить клики)
        categoryGroup.addWidget(categoryBtn);

// 4. ДОБАВЛЯЕМ текст и иконки ПОВЕРХ кнопки (позиция относительно группы)
        categoryBtn.setButtonTexture(new GuiTextureGroup(new ColorRectTexture(ShopColors.BG_BUTTON).setRadius(6),new TextTexture("Weapon")));
        categoryBtn.setHoverTexture(new GuiTextureGroup(new ColorRectTexture(ShopColors.HOV_BUTTON).setRadius(6),new TextTexture("Weapon")));
// 5. Добавляем всю группу в сайдбар
        sidebar.addWidget(categoryGroup);

        addWidget(sidebar);
    }

    private void buildTopBar() {
        WidgetGroup topBar = new WidgetGroup(sidebarWidth, 0, getSizeWidth() - sidebarWidth, topBarHeight);
        topBar.setBackground(new ColorRectTexture(ShopColors.BG_PANEL));
        // Поле поиска
        TextFieldWidget searchField = new TextFieldWidget(20, 6, sidebarWidth - 12, 20, () -> "", text -> {
            // Поиск по тексту
        });
        searchField.setBordered(false);
        searchField.setBackground(new ColorRectAndBorderTexture(ShopColors.BG_PANEL, ShopColors.BORDER, 1).setRadius(4));
        searchField.setTextColor(ShopColors.TEXT_MAIN);
        topBar.addWidget(searchField);

        // Баланс (справа)
        LabelWidget balance = new LabelWidget(topBar.getSizeWidth() - 120, 15, "Баланс: $150 | 💎 12")
                .setTextColor(ShopColors.TEXT_MAIN);
        topBar.addWidget(balance);

        addWidget(topBar);
    }

    private void buildCatalogGrid() {
        catalogGrid = new ShopEntryPanel(sidebarWidth + 20, topBarHeight + 10, getSizeWidth() - sidebarWidth - 40, getSizeHeight() - topBarHeight - 20);

        // Заполняем тестовыми данными
        catalogGrid.addWidget(new ShopEntryCardWidget(280, 120)); // Карточка 1
        catalogGrid.addWidget(new ShopEntryCardWidget(280, 120)); // Карточка 2

        // Вызываем метод, который мы писали ранее, чтобы он расставил их по сетке (Grid Layout)
        catalogGrid.recalculateGrid();

        addWidget(catalogGrid);
    }


    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = sidebarWidth;
        int y = 32;
        int width = getSize().width;

        // graphics.fill(startX, startY, endX, endY, color_ARGB)
        // Горизонтальная линия под шапкой
        graphics.fill(x, y, x + width, y + 1, 0xFFC4C4FF);

        // Вертикальная линия, отделяющая сайдбар
        //graphics.fill(x + 200, y, x + 201, y + getSize().height, ShopColors.BORDER);
    }

    // Обработка ресайза окна Minecraft
    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {

        setSize(screenWidth, screenHeight);
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        clearAllWidgets();
        buildSidebar();
        sidebar.setSizeHeight(screenHeight);
        buildTopBar();
        //buildCatalogGrid();
    }
}