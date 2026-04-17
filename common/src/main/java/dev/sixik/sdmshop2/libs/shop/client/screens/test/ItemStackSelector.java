package dev.sixik.sdmshop2.libs.shop.client.screens.test;

import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.GuiGraphics;
import org.jetbrains.annotations.NotNull;


import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.function.Consumer;

public class ItemStackSelector extends WidgetGroup {

    private final Consumer<ItemStack> onSelect;
    private ItemStack itemStack;
    private DraggableScrollableWidgetGroup grid;
    private TextFieldWidget searchField;
    private boolean isAll = true;
    private ButtonWidget changButton = new ButtonWidget();
    private List<ItemStack> allItems;
    protected int panelSize;

    protected String searchText = "";

    public ItemStackSelector(ItemStack itemStack, Consumer<ItemStack> onSelect) {
        // Вызов суперконструктора с autoAdd = true сам добавит диалог поверх parent
        this.onSelect = onSelect;
        this.itemStack = itemStack;
        panelSize = (int) Math.min((Minecraft.getInstance().getWindow().getGuiScaledWidth()*0.7),Minecraft.getInstance().getWindow().getGuiScaledHeight()*0.7);
        setBackground(new ColorRectAndBorderTexture(ShopColors.BG_PANEL, ShopColors.BORDER, 0));

        setGridItemGetter(InventoryType.Creative);
        //changButton.setButtonTexture(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.BG_BUTTON,ShopColors.BORDER,0).setRightRadius(5),new TextTexture("All")));
    }

    @Override
    public void initWidget() {

        System.out.println("Init widgets ItemStackSelector");
        drawDialogPanel();
        calculateWidgetSize();
        super.initWidget();
    }

    private void drawDialogPanel() {

        Widget img = new WidgetGroup(2,2,50,50);
        img.setBackground(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.ITEM_SElECTOR_BUTTONS,ShopColors.BORDER,1).setRadius(2),new ItemStackTexture(itemStack)));

        // 2. Поле поиска
        searchField = new TextFieldWidget(10, 10, panelSize - 20, 10, null, null) {
            @Override
            protected void onTextChanged(String newTextString) {
                if(searchText.equals(newTextString)) return;
                searchText = newTextString;
                rebuildGrid(newTextString.toLowerCase());
            }
        };
        searchField.setCurrentString(searchText);

        this.addWidget(searchField);
        // Кнопка смены списка предметов
        changButton = new ButtonWidget(panelSize,0,18,18,
                clickData -> {
                    if (isAll) {
                        changButton.setBackground(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.BG_BUTTON,ShopColors.BORDER,0).setRightRadius(5),new TextTexture("All")));
                        setGridItemGetter(InventoryType.Player);
                        rebuildGrid();
                        isAll = !isAll;
                    }else{
                        changButton.setBackground(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.BG_BUTTON,ShopColors.BORDER,0).setRightRadius(5),new TextTexture("Inv")));
                        setGridItemGetter(InventoryType.Creative);
                        rebuildGrid();
                        isAll = !isAll;
                    }
                }
        );
        changButton.setButtonTexture(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.BG_BUTTON,ShopColors.BORDER,0).setRightRadius(5),new TextTexture("Inv")));
        addWidget(changButton);
        // 3. Скролл-панель для сетки предметов
        grid = new DraggableScrollableWidgetGroup();
        grid.setScrollable(true);
        grid.setUseScissor(true); // Критически важно для отсечения невидимых предметов при рендере
        this.addWidget(grid);

        // 4. Первичная сборка сетки (пустая строка = все предметы)
        // rebuildGrid
    }

    public void setGridItemGetter(InventoryType inventoryType) {
        switch (inventoryType) {
            case Player -> {
                setGridItemGetter(() -> {
                    final List<ItemStack> itemStacks = new ObjectArrayList<>();
                    final Inventory inventory = Minecraft.getInstance().player.getInventory();
                    for (int i = 0; i < inventory.getContainerSize(); i++) {
                        final ItemStack item = inventory.getItem(i);
                        if(item.isEmpty()) continue;
                        itemStacks.add(item);
                    }
                    return itemStacks;
                });
            }
            case Creative -> {
                setGridItemGetter(() -> {
                    final List<ItemStack> itemStacks = new ObjectArrayList<>();
                    for (Item item : BuiltInRegistries.ITEM) {
                        if(item == Items.AIR) continue;
                        itemStacks.add(item.getDefaultInstance());
                    }
                    return itemStacks;
                });
            }
        }
    }

    public void setGridItemGetter(Supplier<List<ItemStack>> itemGetter) {
        allItems = itemGetter.get();
    }

    private void rebuildGrid() {
        rebuildGrid(searchText);
    }

    private void rebuildGrid(String searchText) {
        grid.clearAllWidgets();
        grid.setBackground(new ColorRectTexture(ShopColors.BG_ORANGE));

        // Быстрая фильтрация по локализованному названию
        List<ItemStack> filtered = allItems.stream()
                .filter(stack -> stack.getHoverName().getString().toLowerCase().contains(searchText))
                .collect(Collectors.toList());

        // Настройки сетки
        final int itemSize = 18;
        final int padding = 2;
        int columns = grid.getSizeWidth() / (itemSize + padding);
        if (columns <= 0) columns = 1;

        // УДАЛЕНО: grid.setSelfPositionX((panelSize-20*columns)/2);

        // ВЫЧИСЛЯЕМ отступ для центрирования элементов ВНУТРИ сетки
        int totalContentWidth = columns * (itemSize + padding) - padding;
        int startX = Math.max(0, (grid.getSizeWidth() - totalContentWidth) / 2);

        // Математическое построение без вложенных Layout-контейнеров (Hot Path)
        for (int i = 0; i < filtered.size(); i++) {
            ItemStack stack = filtered.get(i);
            int col = i % columns;
            int row = i / columns;

            // Прибавляем startX к позиции каждого предмета
            int x = startX + col * (itemSize + padding);
            int y = row * (itemSize + padding);
            ItemButton button = new ItemButton(x, y, itemSize, stack, this::selectItem);
            grid.addWidget(button);
        }
    }

    /**
     * Пересчитывает параметры виджетов (Размер, Позиция)
     */
    private void calculateWidgetSize() {
        searchField.setSelfPosition(10, 10);
        searchField.setSize(panelSize - 20, 10);

        changButton.setSelfPosition(panelSize, 0);
        changButton.setSize(18, 18);

        grid.setSelfPosition(10, 24);
        grid.setSize(panelSize - 20, panelSize - 26);

        rebuildGrid();
    }

    private void selectItem(ItemStack stack) {
        onSelect.accept(stack);
        parent.removeWidget(this);
    }


    // Легковесная кнопка для предмета.
    private static class ItemButton extends ButtonWidget {

        public ItemButton(int x, int y, int size, ItemStack stack, Consumer<ItemStack> onClick) {
            super(x, y, size, size, click -> onClick.accept(stack));
            this.setBackground(new GuiTextureGroup(new ColorRectTexture(ShopColors.ITEM_SElECTOR_BUTTONS),new ItemStackTexture(stack)));
            // Подсветка при наведении (полупрозрачный белый)
            this.setHoverTexture(new ColorRectTexture(0x44FFFFFF));
            // Тултип с названием предмета
            this.setHoverTooltips(new Component[]{ stack.getHoverName() });
        }

    }

    public void onScreenSizeUpdate(int screenWidth, int screenHeight) {

//        setSize(parent.getSize());
        super.onScreenSizeUpdate(screenWidth, screenHeight);
        int a = Math.min(screenHeight,screenWidth);
        panelSize = (int) (a*0.7);
        calculateWidgetSize();
//        clearAllWidgets();
//        drawDialogPanel();
    }

    public enum InventoryType {
        Player,
        Creative
    }
}

