package dev.sixik.sdmshop2.libs.shop.client.screens.test;

import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.texture.*;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.sixik.sdmshop2.libs.shop.client.ShopColors;
import dev.sixik.sdmshop2.libs.shop.client.textures.ColorRectAndBorderTexture;
import net.minecraft.client.Minecraft;
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
    private boolean isAll = true;
    private ButtonWidget changButton = new ButtonWidget();
    private final List<ItemStack> allItems = new ArrayList<>();
    protected int panelSize;
    protected final int screenWidth = Minecraft.getInstance().getWindow().getGuiScaledWidth();
    protected final int screenHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();

    public ItemStackSelector(WidgetGroup parent, Consumer<ItemStack> onSelect) {
        // Вызов суперконструктора с autoAdd = true сам добавит диалог поверх parent
        super(parent, true);
        this.onSelect = onSelect;
        this.setParentInVisible();
        int a = Math.min(screenHeight,screenWidth);
        panelSize = (int) (a*0.7);
        setSize(panelSize, panelSize);
        setBackground(new ColorRectAndBorderTexture(ShopColors.BG_PANEL, ShopColors.BORDER, 0));
        for (Item item : BuiltInRegistries.ITEM) {
            if (item != Items.AIR) {
                allItems.add(item.getDefaultInstance());
            }
        }
        drawDialogPanel();
        //changButton.setButtonTexture(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.BG_BUTTON,ShopColors.BORDER,0).setRightRadius(5),new TextTexture("All")));
    }

    @Override
    public void initWidget() {
        super.initWidget();
        setSelfPosition((this.parent.getSize().width - this.getSize().width) / 2, (this.parent.getSize().height - this.getSize().height) / 2);
    }

    private void drawDialogPanel() {

        // 2. Поле поиска
        TextFieldWidget searchField = new TextFieldWidget(10, 10, getSizeWidth() - 20, 10,
                null,
                text -> rebuildGrid(text.toLowerCase()));

        this.addWidget(searchField);
        // Кнопка смены списка предметов
        changButton = new ButtonWidget(getSizeWidth(),0,18,18,
                clickData -> {
                    if (isAll) {
                        changButton.setBackground(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.BG_BUTTON,ShopColors.BORDER,0).setRightRadius(5),new TextTexture("All")));
                        allItems.clear();
                        for (ItemStack item : Minecraft.getInstance().player.inventoryMenu.getItems().stream().toList()) {
                            if (item.getItem() != Items.AIR) {
                                allItems.add(item);
                            }
                        }
                        rebuildGrid("");
                        isAll = !isAll;
                    }else{
                        changButton.setBackground(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.BG_BUTTON,ShopColors.BORDER,0).setRightRadius(5),new TextTexture("Inv")));
                        allItems.clear();
                        for (Item item : BuiltInRegistries.ITEM) {
                            if (item != Items.AIR) {
                                allItems.add(item.getDefaultInstance());
                            }
                        }
                        rebuildGrid("");
                        isAll = !isAll;
                    }
                }
        );
        changButton.setButtonTexture(new GuiTextureGroup(new ColorRectAndBorderTexture(ShopColors.BG_BUTTON,ShopColors.BORDER,0).setRightRadius(5),new TextTexture("Inv")));
        addWidget(changButton);
        // 3. Скролл-панель для сетки предметов
        grid = new DraggableScrollableWidgetGroup(10, searchField.getPositionY()+14, panelSize - 20, panelSize - (searchField.getPositionY()+16));
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
        grid.setSelfPositionX((panelSize-20*columns)/2);
        // Математическое построение без вложенных Layout-контейнеров (Hot Path)
        for (int i = 0; i < filtered.size(); i++) {
            ItemStack stack = filtered.get(i);
            int col = i % columns;
            int row = i / columns;

            int x = col * (itemSize + padding);
            int y = row * (itemSize + padding);
            ItemButton button = new ItemButton(x, y, itemSize, stack, this::selectItem);
            //button.setBackground(new ItemStackTexture(stack));
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
            this.setBackground(new GuiTextureGroup(new ColorRectTexture(ShopColors.ITEM_SElECTOR_BUTTONS),new ItemStackTexture(stack)));
            // Подсветка при наведении (полупрозрачный белый)
            this.setHoverTexture(new ColorRectTexture(0x44FFFFFF));
            // Тултип с названием предмета
            this.setHoverTooltips(new Component[]{ stack.getHoverName() });
        }

    }

}

