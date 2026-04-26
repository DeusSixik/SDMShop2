package dev.sixik.sdmshop2.libs.shop.client.config.component_selector;

import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.layout.Align;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.platform.Window;
import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.SDMTextLabel;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.limiter.LimiterComponent;
import dev.sixik.sdmshop2.libs.shop.components.promo.effects.DiscountComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Consumer;

public class ComponentSelectionMenu {

    public static DialogWidget showComponentSelector(WidgetGroup parent, String title, Consumer<ShopComponent> onSelected) {
        Window mcWindow = Minecraft.getInstance().getWindow();

        final int sw = mcWindow.getGuiScaledWidth();
        final int sh = mcWindow.getGuiScaledHeight();
        DialogWidget dialog = new DialogWidget(0, 0, sw, sh);
        dialog.setClientSideWidget();
        parent.addWidget(dialog);


        dialog.setClickClose(true);
        dialog.setParentInVisible();


        final int margin = 10;

        final int availW = Math.max(1, sw - margin * 2);
        final int availH = Math.max(1, sh - margin * 2);

        final int winWidth = (int) (availW * 0.95f);
        final int winHeight = (int) (availH * 0.95f);

        // Увеличиваем размер окна под плиточный интерфейс

        WidgetGroup window = new WidgetGroup(0, 0, winWidth, winHeight);
        window.setAlign(Align.CENTER);

        WidgetGroup titleGroup = new WidgetGroup(0, 0, winWidth, 15);
        titleGroup.setBackground(new GuiTextureGroup(
                ColorPattern.RED.rectTexture().setTopRadius(5f),
                ColorPattern.GRAY.borderTexture(-1).setTopRadius(5f),
                new TextTexture(title).setWidth(winWidth).setDropShadow(false).setType(TextTexture.TextType.ROLL)
        ));
        window.addWidget(titleGroup);

        WidgetGroup contentGroup = new WidgetGroup(0, 15, winWidth, winHeight - 15);
        contentGroup.setBackground(new GuiTextureGroup(
                ColorPattern.BLACK.rectTexture().setBottomRadius(5f),
                ColorPattern.GRAY.borderTexture(-1).setBottomRadius(5f)
        ));
        window.addWidget(contentGroup);
        dialog.addWidget(window);

        int contentWidth = contentGroup.getSizeWidth();
        int contentHeight = contentGroup.getSizeHeight();

        DraggableScrollableWidgetGroup scroll = new DraggableScrollableWidgetGroup(5, 5, contentWidth - 10, contentHeight - 30);
        scroll.setYScrollBarWidth(4);
        scroll.setYBarStyle(null, ColorPattern.WHITE.rectTexture().setRadius(2));

        WidgetGroup gridContainer = new WidgetGroup(0, 0, contentWidth - 15, 0);
        // Отключаем Layout, мы будем ставить координаты вручную
        gridContainer.setLayout(Layout.NONE);
        gridContainer.setDynamicSized(true);

        // --- ЛОГИКА СЕТКИ ---
        int tileW = 100; // Ширина плитки
        int tileH = 110; // Высота плитки
        int spacing = 5; // Расстояние между плитками
        // Вычисляем, сколько колонок влезет в ширину контейнера
        int columns = Math.max(1, (contentWidth - 15) / (tileW + spacing));

        int index = 0;
        for (ShopComponent comp : getAllAvailableComponents()) {
            // Математика сетки
            int col = index % columns;
            int row = index / columns;

            int x = col * (tileW + spacing);
            int y = row * (tileH + spacing);

            Widget tile = createTileWidget(x, y, tileW, tileH, comp, () -> {
                dialog.close();
                if (onSelected != null) onSelected.accept(comp);
            });

            gridContainer.addWidget(tile);
            index++;
        }

        // Принудительно задаем ширину, чтобы группа правильно посчитала свою финальную высоту
        gridContainer.setSizeWidth(contentWidth - 15);

        scroll.addWidget(gridContainer);
        contentGroup.addWidget(scroll);

        DialogWidget.createButton(contentGroup, (contentWidth - 60) / 2, contentHeight - 20, 60, 15, "ldlib.gui.tips.cancel", () -> {
            dialog.close();
            if (onSelected != null) onSelected.accept(null);
        });

        return dialog;
    }

    private static Iterable<ShopComponent> getAllAvailableComponents() {
        return List.of(new DiscountComponent(), new LimiterComponent());
    }

    private static Widget createTileWidget(int x, int y, int w, int h, ShopComponent comp, Runnable onClick) {
        // Создаем интерактивный контейнер
        WidgetGroup tile = new WidgetGroup(x, y, w, h) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                // Если клик левой кнопкой мыши был внутри этой плитки
                if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
                    Widget.playButtonClickSound();
                    onClick.run();
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }
        };

        // Делаем фон (как у кнопок LDLib)
        tile.setBackground(new GuiTextureGroup(
                ColorPattern.T_GRAY.rectTexture().setRadius(4f),
                ColorPattern.GRAY.borderTexture(-1).setRadius(4f)
        ));

        Font font = Minecraft.getInstance().font;

        // --- 1. ИМЯ (Сверху по центру) ---
        // Берем ID и отрезаем "minecraft:", чтобы текст был короче
        String name = comp.getType().getId().getPath();
        SDMTextLabel nameLabel = new SDMTextLabel(Component.literal(name));
        nameLabel.setSelfPosition((w - font.width(name)) / 2, 5);
        tile.addWidget(nameLabel);

        // --- 2. ИКОНКА (По центру) ---
        // ЗАМЕТКА: Замени ColorPattern.BLACK на текстуру твоего компонента (например comp.getIcon())
        IGuiTexture iconTexture = new ItemStackTexture(Items.ICE);
        ImageWidget icon = new ImageWidget((w - 32) / 2, 25, 32, 32, iconTexture);
        tile.addWidget(icon);

        // --- 3. ОПИСАНИЕ (Снизу, мелким шрифтом) ---
        // ЗАМЕТКА: Замени на реальное описание из компонента
        Component desc = Component.literal("Добавляет лимит...");
        SDMTextLabel descLabel = new SDMTextLabel(desc);
        descLabel.setScale(0.7f); // Сжимаем текст до 70%, чтобы влезло больше слов

        // Считаем ширину ужатого текста, чтобы отцентрировать его
        int scaledWidth = (int) (font.width(desc) * 0.7f);
        descLabel.setSelfPosition((w - scaledWidth) / 2, h - 20);

        tile.addWidget(descLabel);

        return tile;
    }
}
