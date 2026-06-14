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

public class SDMMultiLineTextLabel extends Widget {

    @Getter
    protected Component text;

    @Getter
    protected float scale = 1.0f;

    public int color = 0xFFFFFFFF;

    // Настройки виджета
    protected boolean autoScale = true;
    protected int maxLines = 2; // Максимальное количество строк по умолчанию

    // Внутренние закэшированные данные для рендера
    protected float currentRenderScale = 1.0f;
    protected List<FormattedCharSequence> renderLines;
    protected int renderY = 0;

    public SDMMultiLineTextLabel(int x, int y, int width, int height, Component text, int maxLines) {
        super(x, y, width, height);
        this.text = text;
        this.maxLines = maxLines;
        recalculate();
    }

    public SDMMultiLineTextLabel setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
        recalculate();
        return this;
    }

    public SDMMultiLineTextLabel setMaxLines(int maxLines) {
        this.maxLines = maxLines;
        recalculate();
        return this;
    }

    public void setText(Component text) {
        this.text = text;
        recalculate();
    }

    public void setScale(float scale) {
        this.scale = scale;
        recalculate();
    }

    @Override
    public void setSize(Size size) {
        super.setSize(size);
        recalculate();
    }

    /**
     * Основная логика: разбивает текст на строки и считает идеальный масштаб
     */
    protected void recalculate() {
        if (text == null || getSizeWidth() <= 0 || getSizeHeight() <= 0) return;

        var font = Minecraft.getInstance().font;

        if (!autoScale) {
            // Если авто-масштаб выключен, просто рубим текст по ширине виджета
            currentRenderScale = this.scale;
            int virtualWidth = (int) (getSizeWidth() / currentRenderScale);
            var allLines = font.split(text, virtualWidth);

            // Ограничиваем количество строк (обрезаем лишнее)
            renderLines = allLines.subList(0, Math.min(allLines.size(), maxLines));

            // Центрируем по вертикали
            float totalHeight = renderLines.size() * font.lineHeight * currentRenderScale;
            renderY = (int) ((getSizeHeight() - totalHeight) / 2.0f);
            return;
        }

        // --- БИНАРНЫЙ ПОИСК ВМЕСТИМОСТИ ---
        // Ищем минимальную виртуальную ширину, при которой текст разобьется на <= maxLines строк.
        int textFullWidth = font.width(text);
        int low = getSizeWidth(); // Не имеет смысла делать виртуальную ширину меньше физической
        int high = Math.max(low, textFullWidth); // Максимум - ширина текста в одну длинную строку
        int bestVirtualWidth = high;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            var lines = font.split(text, mid);

            if (lines.size() <= maxLines) {
                bestVirtualWidth = mid; // Влезло! Но попробуем уменьшить ширину (увеличить масштаб)
                high = mid - 1;
            } else {
                low = mid + 1; // Не влезло (строк слишком много), нужна виртуальная ширина больше
            }
        }

        // Получаем итоговые строки с найденной идеальной шириной
        renderLines = font.split(text, bestVirtualWidth);
        if (renderLines.size() > maxLines) {
            renderLines = renderLines.subList(0, maxLines);
        }

        // Вычисляем масштаб по X. Ищем самую длинную строку из получившихся.
        int maxLineWidth = 1; // Защита от деления на 0
        for (var line : renderLines) {
            maxLineWidth = Math.max(maxLineWidth, font.width(line));
        }

        float scaleX = (float) getSizeWidth() / maxLineWidth;
        float scaleY = (float) getSizeHeight() / (renderLines.size() * font.lineHeight);

        // Итоговый масштаб — наименьший из необходимых, но не превышающий базовый scale
        currentRenderScale = Math.min(this.scale, Math.min(scaleX, scaleY));

        // Вычисляем Y для вертикального центрирования получившегося блока текста
        float totalHeight = renderLines.size() * font.lineHeight * currentRenderScale;
        renderY = (int) ((getSizeHeight() - totalHeight) / 2.0f);
    }

    @Override
    public void drawInBackground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        if (renderLines == null || renderLines.isEmpty()) return;

        var font = Minecraft.getInstance().font;
        var position = getPosition();

        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        // Смещаем рендер в нужную позицию с учетом центрирования по Y
        poseStack.translate(position.getX(), position.getY() + renderY, 0);
        poseStack.scale(currentRenderScale, currentRenderScale, 1.0f);

        // Отрисовываем каждую строку друг под другом
        int currentY = 0;
        for (FormattedCharSequence line : renderLines) {
            graphics.drawString(font, line, 0, currentY, color, false);
            currentY += font.lineHeight; // Опускаемся на следующую строку
        }

        poseStack.popPose();
    }
}