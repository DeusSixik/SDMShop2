package dev.sixik.sdmshop2.libs.shop.client.screens.test;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.platform.Window;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.screens.ShopEntryPanel;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import net.minecraft.client.Minecraft;

public class ShopMainPanel extends WidgetGroup {

    private final int sidebarWidth = 200;
    private final int topBarHeight = 40;

    private WidgetGroup sidebar;
    private WidgetGroup mainContent;
    private ShopEntryPanel catalogGrid; // Тот самый скролл-виджет, который мы писали ранее

    public ShopMainPanel() {
        final Minecraft minecraft = Minecraft.getInstance();
        final Window window = minecraft.getWindow();

        final int w = window.getGuiScaledWidth();
        final int h = window.getGuiScaledHeight();

        setSize(w - w / 8, h - h / 6);

        // Главный фон экрана (--bg-base)
        setBackground(new ColorRectTexture(ShopColors.BG_BASE));

        buildSidebar();
        buildTopBar();
        buildCatalogGrid();
    }

    private void buildSidebar() {
        sidebar = new WidgetGroup(0, 0, sidebarWidth, getSizeHeight());
        // Фон панели с правой рамкой
        sidebar.setBackground(new ColorRectAndBorderTexture(ShopColors.BG_PANEL, ShopColors.BORDER, 0)
                .setRightRadius(0)); // Радиус только если нужен, иначе просто прямая заливка

        // Заголовок
        LabelWidget title = new LabelWidget(20, 20, "Магазин").setTextColor(ShopColors.TEXT_MAIN);
        // В LDLib LabelWidget сам считает свой размер, но можно увеличить масштаб через матрицу или использовать TextTextureWidget
        sidebar.addWidget(title);

        WidgetGroup categoryGroup = new WidgetGroup(15, 50, sidebarWidth - 30, 25);

// 2. Создаем саму кнопку внутри группы (размер равен группе, позиция 0,0)
        ButtonWidget categoryBtn = new ButtonWidget(0, 0, categoryGroup.getSizeWidth(), categoryGroup.getSizeHeight(), cd -> {
            System.out.println("Выбрана категория: Оружие");
        });

// Настраиваем фон и ховер именно на кнопке
        categoryBtn.setBackground(new ColorRectTexture(0x00000000).setRadius(6)); // Прозрачная
        categoryBtn.setHoverTexture(new ColorRectTexture(ShopColors.BG_HOVER).setRadius(6));
        categoryBtn.setDrawBackgroundWhenHover(false);

// 3. Добавляем в группу СНАЧАЛА кнопку (она будет фоном и ловить клики)
        categoryGroup.addWidget(categoryBtn);

// 4. ДОБАВЛЯЕМ текст и иконки ПОВЕРХ кнопки (позиция относительно группы)
        categoryGroup.addWidget(new LabelWidget(10, 7, "⚔️ Оружие и Броня").setTextColor(ShopColors.TEXT_MAIN));

// 5. Добавляем всю группу в сайдбар
        sidebar.addWidget(categoryGroup);

        addWidget(sidebar);
    }

    private void buildTopBar() {
        WidgetGroup topBar = new WidgetGroup(sidebarWidth, 0, getSizeWidth() - sidebarWidth, topBarHeight);

        // Поле поиска
        TextFieldWidget searchField = new TextFieldWidget(20, 10, 200, 20, () -> "", text -> {
            // Поиск по тексту
        });
        searchField.setBackground(new ColorRectAndBorderTexture(ShopColors.BG_PANEL, ShopColors.BORDER, 1).setRadius(4));
        searchField.setTextColor(ShopColors.TEXT_MAIN);
        topBar.addWidget(searchField);

        // Баланс (справа)
        LabelWidget balance = new LabelWidget(topBar.getSizeWidth() - 150, 15, "Баланс: $150 | 💎 12")
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

    // Обработка ресайза окна Minecraft
    @Override
    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        setSize(screenWidth, screenHeight);
        sidebar.setSizeHeight(screenHeight);
        // Обновляем размеры остальных зон...
    }
}