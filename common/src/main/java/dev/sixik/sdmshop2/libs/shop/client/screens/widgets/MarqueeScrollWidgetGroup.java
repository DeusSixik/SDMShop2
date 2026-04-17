package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

public class MarqueeScrollWidgetGroup extends DraggableScrollableWidgetGroup {

    // Субпиксельный аккумулятор для плавности
    private float exactOffset = 0f;
    // Скорость скролла (пикселей за кадр). Можно делать < 1.0f для медленного движения.
    private final float scrollSpeed;
    private boolean vec = true;

    public MarqueeScrollWidgetGroup(int x, int y, int width, int height, float scrollSpeed) {
        super(x, y, width, height);
        this.scrollSpeed = scrollSpeed;

        // Отключаем ручной скролл колесиком мыши (опционально, если это просто дисплей)
        this.setScrollable(false);
        // Включаем обрезку контента, выходящего за рамки (критично для бегущей строки)
        this.setUseScissor(true);
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // 🔥 Горячий путь (Hot Path): Выполняется каждый кадр рендера (60+ FPS)

        // Проверяем наведение мыши без аллокаций
        if (isMouseOverElement(mouseX, mouseY)) {

            // Вычисляем, насколько контент шире самой панели
            int maxScroll = Math.max(0, getMaxWidth() - getSizeWidth());
            int minScroll = Math.min(0, getMaxWidth() - getSizeWidth());

            if (vec) {
                exactOffset += scrollSpeed;

                // Переключение вектора движения (+20px для микро-паузы перед сбросом)
                if (exactOffset > maxScroll + 20) {
                    vec = false;
                }

                // Применяем смещение (каст во float -> int отсекает дроби, но сохраняет плавность)
                setScrollXOffset((int) exactOffset);
            } else {
                exactOffset -= scrollSpeed;

                // Переключение вектора движения
                if (exactOffset < minScroll) {
                    vec = true;
                }

                // Применяем смещение (каст во float -> int отсекает дроби, но сохраняет плавность)
                setScrollXOffset((int) exactOffset);
            }
        } else {
            // Плавный (или мгновенный) возврат в начало, если убрали курсор
            if (exactOffset > 0) {
                exactOffset = 0;
                setScrollXOffset(0);
            }
        }

        // Обязательно вызываем родительский метод для отрисовки фона и применения смещений
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
    }
}