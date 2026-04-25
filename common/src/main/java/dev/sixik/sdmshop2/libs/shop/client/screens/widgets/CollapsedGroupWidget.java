package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.layout.Layout;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.client.config.ComponentConfigurationWidget;
import dev.sixik.sdmshop2.libs.shop.components.api.IComponentType;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ShopComponentRegistry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CollapsedGroupWidget extends WidgetGroup {

    // Палитра цветов для разных уровней глубины (Стиль IDE)
    private static final int[] DEPTH_COLORS = new int[]{
            0xFF5555FF, // Синий
            0xFF55FF55, // Зеленый
            0xFFFFFF55, // Желтый
            0xFFFF55FF, // Пурпурный
            0xFF55FFFF, // Голубой
            0xFFFF9955  // Оранжевый
    };

    @Setter
    protected boolean canCollapse = true;

    @Getter
    protected boolean isCollapsed = true;

    @Getter @Setter
    protected Component title;

    @Getter @Setter
    protected int headerHeight = 16;

    // Размер "табуляции" (отступа слева) для вложенных элементов
    @Getter @Setter
    protected int indentSize = 12;

    public CollapsedGroupWidget(Component title, int width) {
        super(0, 0, width, 16);
        this.title = title;

        this.setDynamicSized(true);
        this.setLayout(Layout.VERTICAL_LEFT);
        this.setLayoutPadding(4);
    }



    /**
     * Меняем состояние и обновляем видимость дочерних элементов
     */
    public void setCollapsed(boolean collapsed) {
        if (this.isCollapsed == collapsed) return;
        this.isCollapsed = collapsed;

        for (Widget widget : widgets) {
            widget.setVisible(!collapsed);
        }

        recomputeLayout();
        recomputeSize();
    }

    /**
     * Вычисляем уровень вложенности, поднимаясь по дереву родителей
     */
    private int calculateDepth() {
        int depth = 0;
        WidgetGroup parent = this.getParent();
        while (parent != null) {
            if (parent instanceof CollapsedGroupWidget) {
                depth++;
            }
            parent = parent.getParent();
        }
        return depth;
    }

    /**
     * Перехватываем добавление виджетов, чтобы они сразу получали правильный статус видимости
     */
    @Override
    public WidgetGroup addWidget(int index, Widget widget) {
        int availableWidth = this.getSizeWidth() - this.indentSize;
        widget.setSizeWidth(availableWidth);

        super.addWidget(index, widget);
        widget.setVisible(!isCollapsed);
        return this;
    }

    /**
     * Рисуем шапку (фон, текст и стрелочку)
     */
    @Override
    public void drawInBackground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int x = getPositionX();
        int y = getPositionY();
        int width = getSizeWidth();

        boolean isHovered = canCollapse && isMouseOver(x, y, width, headerHeight, mouseX, mouseY);
        int headerColor = isHovered ? 0xFF555555 : 0xFF333333;
        graphics.fill(x, y, x + width, y + headerHeight, headerColor);

        Font font = Minecraft.getInstance().font;
        int textY = y + (headerHeight - font.lineHeight) / 2 + 1;

        graphics.drawString(font, title, x + 4, textY, 0xFFFFFF, false);

        String arrow = isCollapsed ? "▶" : "▼";
        graphics.drawString(font, arrow, x + width - 12, textY, 0xAAAAAA, false);

        if (!isCollapsed && !widgets.isEmpty()) {
            int depth = calculateDepth();
            int color = DEPTH_COLORS[depth % DEPTH_COLORS.length];

            int halfIndent = indentSize / 2;
            int lineX = x + halfIndent;
            int startY = y + headerHeight;
            int endY = y + getSizeHeight() - 5;

            // Вертикальная линия
            graphics.fill(lineX, startY, lineX + 1, endY, color);

            for (Widget widget : widgets) {
                if (!widget.isVisible()) continue;

                int widgetY = widget.getPositionY() + (headerHeight / 2);

                graphics.fill(lineX, widgetY, lineX + halfIndent, widgetY + 1, color);
            }
        }
    }

    /**
     * Обработка клика по шапке
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = getPositionX();
        int y = getPositionY();

        if (canCollapse && button == 0 && isMouseOver(x, y, getSizeWidth(), headerHeight, mouseX, mouseY)) {
            setCollapsed(!isCollapsed);
            Widget.playButtonClickSound();
            return true;
        }

        if (isCollapsed) return false;

        return super.mouseClicked(mouseX, mouseY, button);
    }

    /**
     * Переопределяем Layout, чтобы дети не налезали на шапку
     */
    @Override
    protected void recomputeLayout() {
        Layout layout = getLayout();
        if (layout != Layout.NONE) {

            /*
                Начинаем расставлять виджеты НЕ с 0, а под шапкой!
             */
            var lastPosition = new Position(indentSize, headerHeight);
            int padding = getLayoutPadding();

            /*
                Переопределяем логику для вертикальных списков
             */
            if (layout == Layout.VERTICAL_LEFT || layout == Layout.VERTICAL_CENTER) {
                for (var widget : widgets) {
                    if (!widget.isVisible()) continue; // Игнорируем скрытые элементы

                    lastPosition = lastPosition.addY(padding);
                    if (layout == Layout.VERTICAL_CENTER) {
                        widget.setSelfPosition(lastPosition.add((getSizeWidth() - widget.getSizeWidth()) / 2, 0));
                    } else {
                        widget.setSelfPosition(lastPosition);
                    }
                    lastPosition = lastPosition.add(0, widget.getSizeHeight());
                }
            }
        }
    }

    /**
     * Динамическое изменение размера (сворачивание хитбокса)
     */
    @Override
    protected Size computeDynamicSize() {
        if (isCollapsed) {
            return new Size(getSizeWidth(), headerHeight);
        }

        Position selfPosition = getPosition();
        // Начинаем с высоты шапки
        int currentHeight = headerHeight;

        for (Widget widget : widgets) {
            if (!widget.isVisible()) continue;

            Position childEnd = widget.getPosition().add(widget.getSize()).subtract(selfPosition);
            // Ищем только самую нижнюю точку по Y
            if (childEnd.y > currentHeight) {
                currentHeight = childEnd.y;
            }
        }

        return new Size(getSizeWidth(), currentHeight + 5);
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);

        int availableWidth = size.width - this.indentSize;

        for (Widget widget : widgets) {
            widget.setSizeWidth(availableWidth);
        }
    }

    @Override
    public void drawInForeground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);

        int x = getPositionX();
        int y = getPositionY();

        if (isMouseOver(x, y, getSizeWidth(), headerHeight, mouseX, mouseY) && gui != null && gui.getModularUIGui() != null) {

            for (Widget widget : widgets) {
                if (widget instanceof ComponentConfigurationWidget configWidget) {
                    ShopComponent component = configWidget.getComponent();

                    if (component != null) {
                        List<Component> tooltip = new ArrayList<>();
                        tooltip.add(Component.translatable("client.shop.component.editor.json.preivew")); // §e - желтый цвет

                        try {
                            JsonObject json = ShopComponentRegistry.toJson(component);
                            String formattedJson = SDMShop2.GSON.toJson(json);
                            for (String line : formattedJson.split("\n")) {
                                tooltip.add(Component.literal("§7" + line.replace("  ", " ")));
                            }
                        } catch (Exception e) {
                            tooltip.add(Component.translatable("client.shop.component.editor.json.generation_error"));
                        }

                        gui.getModularUIGui().setHoverTooltip(tooltip, ItemStack.EMPTY, null, null);
                    }
                    break;
                }
            }
        }
    }
}
