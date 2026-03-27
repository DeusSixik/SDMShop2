package dev.sixik.sdmshop2.libs.shop.client.screens.test;

import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;


import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.function.Consumer;

public class ItemStackSelector extends DialogWidget {

    private final Consumer<ItemStack> onSelect;
    private  DraggableScrollableWidgetGroup grid;
    private List<ItemStack> allItems;

    public ItemStackSelector(WidgetGroup parent, Consumer<ItemStack> onSelect) {
        // Вызов суперконструктора с autoAdd = true сам добавит диалог поверх parent
        super(parent, true);
        this.setSize(220, 240);
        this.onSelect = onSelect;
        this.setParentInVisible();
        this.setBackground(new ColorRectAndBorderTexture(ShopColors.BG_PANEL, ShopColors.BORDER, 0));

        drawDialogPanel();
    }

    private void drawDialogPanel() {
        // 1. Кешируем все предметы один раз при открытии диалога
        allItems = new ArrayList<>();
        // Если у тебя Fabric или версия 1.20+, замени ForgeRegistries.ITEMS на BuiltInRegistries.ITEM
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                allItems.add(item.getDefaultInstance());
            }
        }
        // 2. Поле поиска
        TextFieldWidget searchField = new TextFieldWidget(10, 10, getSizeWidth() - 20, 10,
                null,
                text -> rebuildGrid(text.toLowerCase()));

        this.addWidget(searchField);
        // 3. Скролл-панель для сетки предметов
        grid = new DraggableScrollableWidgetGroup(10, 30, 200, 200);
        grid.setScrollable(true);
        grid.setUseScissor(true); // Критически важно для отсечения невидимых предметов при рендере
        this.addWidget(grid);

        // 4. Первичная сборка сетки (пустая строка = все предметы)
        rebuildGrid("");
    }

    private void rebuildGrid(String searchText) {
        grid.clearAllWidgets();

        // Быстрая фильтрация по локализованному названию
        List<ItemStack> filtered = allItems.stream()
                .filter(stack -> stack.getHoverName().getString().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        // Настройки сетки
        final int itemSize = 18;
        final int padding = 2;
        int columns = grid.getSizeWidth() / (itemSize + padding);
        if (columns <= 0) columns = 1;

        // Математическое построение без вложенных Layout-контейнеров (Hot Path)
        for (int i = 0; i < filtered.size(); i++) {
            ItemStack stack = filtered.get(i);
            int col = i % columns;
            int row = i / columns;

            int x = col * (itemSize + padding);
            int y = row * (itemSize + padding);
            ItemButton button = new ItemButton(x, y, itemSize, stack, this::selectItem);
            button.setBackground(new ItemStackTexture(stack));
            grid.addWidget(button);
        }
    }

    private void selectItem(ItemStack stack) {
        onSelect.accept(stack);
        this.close(); // Встроенный метод DialogWidget для закрытия всплывающего окна
    }


    // Легковесная кнопка для предмета.
    private static class ItemButton extends ButtonWidget {

        public ItemButton(int x, int y, int size, ItemStack stack, Consumer<ItemStack> onClick) {
            super(x, y, size, size, click -> onClick.accept(stack));
            // Подсветка при наведении (полупрозрачный белый)
            this.setHoverTexture(new IGuiTexture[]{ new ColorRectTexture(0x44FFFFFF) });
            // Тултип с названием предмета
            this.setHoverTooltips(new Component[]{ stack.getHoverName() });
        }

    }

}

