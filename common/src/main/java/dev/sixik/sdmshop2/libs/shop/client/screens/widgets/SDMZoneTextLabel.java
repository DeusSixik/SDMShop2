package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import lombok.Getter;
import lombok.NonNull;

import java.util.List;

public class SDMZoneTextLabel extends Widget {

    @Getter
    protected Component text;

    public int color = 0xFFFFFFFF;

    // Внутренние закэшированные данные для рендера
    protected float optimalScale = 1.0f;
    protected List<FormattedCharSequence> renderLines;
    protected int renderY = 0;

    public SDMZoneTextLabel(int x, int y, int width, int height, Component text) {
        super(x, y, width, height);
        this.text = text;
        recalculate();
    }

    public void setText(Component text) {
        this.text = text;
        recalculate();
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
        recalculate();
    }

    /**
     * Алгоритм подбора идеального масштаба и количества строк под заданную ЗОНУ (width x height)
     */
    protected void recalculate() {
        if (text == null || getSizeWidth() <= 0 || getSizeHeight() <= 0) return;

        var font = Minecraft.getInstance().font;

        // Диапазон поиска масштаба (от 0.1 до 10-кратного увеличения)
        float lowScale = 0.1f;
        float highScale = 1.0f;

        float bestScale = lowScale;
        List<FormattedCharSequence> bestLines = null;

        // Бинарный поиск: за 20 итераций мы с точностью до пикселя найдем идеальный размер.
        // Это очень быстрая операция (не вызывает лагов)
        for (int i = 0; i < 20; i++) {
            float midScale = lowScale + (highScale - lowScale) / 2.0f;

            // Какая "виртуальная" ширина доступна майнкрафту при таком масштабе?
            int virtualWidth = (int) (getSizeWidth() / midScale);

            // Защита от слишком огромного масштаба (когда в ширину не влезает даже 1 пиксель)
            if (virtualWidth < 1) {
                highScale = midScale;
                continue;
            }

            // Майнкрафт сам бьет текст на строки под эту виртуальную ширину
            var lines = font.split(text, virtualWidth);

            // Считаем, какую высоту займут эти строки при нашем масштабе
            float totalHeight = lines.size() * font.lineHeight * midScale;

            // Проверка: иногда font.split не переносит длинное слово без пробелов,
            // поэтому мы проверяем, не вылезает ли самая длинная строка за нашу зону.
            boolean widthFits = true;
            for (var line : lines) {
                // +1 добавлен для компенсации погрешности округления float
                if (font.width(line) * midScale > getSizeWidth() + 1) {
                    widthFits = false;
                    break;
                }
            }

            // Если текст по высоте и по ширине полностью влезает в коробку
            if (totalHeight <= getSizeHeight() && widthFits) {
                // Это хороший вариант! Сохраняем его и пробуем сделать шрифт ЕЩЕ БОЛЬШЕ
                bestScale = midScale;
                bestLines = lines;
                lowScale = midScale;
            } else {
                // Не влезает (слишком высоко или слово вылезло). Делаем масштаб меньше.
                highScale = midScale;
            }
        }

        // Fallback-защита (на случай пустого текста и т.д.)
        if (bestLines == null) {
            bestLines = font.split(text, getSizeWidth());
            bestScale = 1.0f;
        }

        this.optimalScale = bestScale;
        this.renderLines = bestLines;

        // Центрируем полученный блок текста по вертикали (чтобы он был ровно посередине коробки)
        float totalRenderHeight = renderLines.size() * font.lineHeight * optimalScale;
        this.renderY = (int) ((getSizeHeight() - totalRenderHeight) / 2.0f);
    }

    @Override
    public void drawInBackground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        if (renderLines == null || renderLines.isEmpty()) return;

        var font = Minecraft.getInstance().font;
        var position = getPosition();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // Сдвигаем матрицу к позиции виджета + центрирование по Y
        poseStack.translate(position.getX(), position.getY() + renderY, 0);
        poseStack.scale(optimalScale, optimalScale, 1.0f);

        // Рисуем все строки одну под другой
        int currentY = 0;
        for (var line : renderLines) {
            graphics.drawString(font, line, 0, currentY, color, false);
            currentY += font.lineHeight; // Шаг на следующую строку
        }

        poseStack.popPose();
    }
}
