package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import lombok.Getter;
import lombok.NonNull;

public class SDMTextLabel extends Widget {

    @Getter
    protected Component text;

    @Getter
    protected float scale = 1.0f;

    public int color = 0xFFFFFFFF;

    // Флаг, определяющий, должен ли текст сжиматься под размеры виджета
    protected boolean autoScale = false;

    // Вспомогательные переменные для авто-скейлинга
    protected float currentRenderScale = 1.0f;
    protected int renderX = 0;
    protected int renderY = 0;

    public SDMTextLabel(Component text) {
        this(0, 0, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
    }

    public SDMTextLabel(int x, int y, Component text) {
        this(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
    }

    public SDMTextLabel(Position selfPosition, Size size, Component text) {
        super(selfPosition, size);
        this.text = text;
        recalculateAutoScale(); // Считаем масштаб при создании, если задан жесткий размер
    }

    public SDMTextLabel(int x, int y, int width, int height, Component text) {
        super(x, y, width, height);
        this.text = text;
        recalculateAutoScale();
    }

    /**
     * Включает или выключает режим автоматического масштабирования.
     * Если включено, текст будет сжиматься, чтобы влезть в заданный width и height.
     */
    public SDMTextLabel setAutoScale(boolean autoScale) {
        this.autoScale = autoScale;
        recalculateAutoScale();
        return this;
    }

    public void setText(Component text) {
        this.text = text;
        if (!autoScale) {
            // Если авто-масштабирование выключено, меняем размер виджета под текст
            int newWidth = (int) (Minecraft.getInstance().font.width(text) * scale);
            int newHeight = (int) (Minecraft.getInstance().font.lineHeight * scale);
            this.setSize(newWidth, newHeight);
        } else {
            // Если включено, пересчитываем масштаб, чтобы текст влез в текущие размеры виджета
            recalculateAutoScale();
        }
    }

    public void setScale(float scale) {
        this.scale = scale;
        if (!autoScale) {
            int newWidth = (int) (Minecraft.getInstance().font.width(text) * scale);
            int newHeight = (int) (Minecraft.getInstance().font.lineHeight * scale);
            this.setSize(newWidth, newHeight);
        } else {
            recalculateAutoScale();
        }
    }

    /**
     * Переопределяем метод изменения размера, чтобы пересчитывать масштаб,
     * если размер виджета изменился извне (например, лайаут-менеджером).
     */
    @Override
    public void setSize(Size size) {
        super.setSize(size);
        if (autoScale) {
            recalculateAutoScale();
        }
    }

    /**
     * Вычисляет нужный масштаб (currentRenderScale) и позицию для центрирования (по желанию).
     */
    protected void recalculateAutoScale() {
        if (!autoScale || text == null) {
            currentRenderScale = this.scale;
            return;
        }

        int textWidth = Minecraft.getInstance().font.width(text);
        int textHeight = Minecraft.getInstance().font.lineHeight;

        if (textWidth == 0 || textHeight == 0) return;

        // Вычисляем максимально возможный масштаб по X и Y
        float scaleX = (float) this.getSizeWidth() / textWidth;
        float scaleY = (float) this.getSizeHeight() / textHeight;

        // Берем минимальный масштаб, чтобы текст не обрезался ни по одной из осей.
        // Ограничиваем сверху заданным scale (или 1.0f), чтобы текст не увеличивался,
        // если коробка слишком большая, а только сжимался, если не влезает.
        currentRenderScale = Math.min(this.scale, Math.min(scaleX, scaleY));

        // Вычисляем смещение для центрирования текста по вертикали (опционально)
        // Если хочешь выравнивание по левому верхнему краю, просто оставь 0
        float scaledHeight = textHeight * currentRenderScale;
        renderY = (int) ((this.getSizeHeight() - scaledHeight) / 2.0f);

        // Для выравнивания по левому краю:
        renderX = 0;

        // Если нужно выравнивание по центру по горизонтали, раскомментируй эту строку:
        // float scaledWidth = textWidth * currentRenderScale;
        // renderX = (int) ((this.getSizeWidth() - scaledWidth) / 2.0f);
    }

    @Override
    public void drawInBackground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        var position = getPosition();

        if (this.currentRenderScale == 1.0f && !autoScale) {
            graphics.drawString(Minecraft.getInstance().font, text, position.getX(), position.getY(), color);
            return;
        }

        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();

        // Сдвигаемся к позиции виджета + смещение для выравнивания (renderX/Y)
        poseStack.translate(position.getX() + renderX, position.getY() + renderY, 0);

        // Применяем вычисленный масштаб
        poseStack.scale(currentRenderScale, currentRenderScale, 1.0f);

        graphics.drawString(Minecraft.getInstance().font, text, 0, 0, color, false);

        poseStack.popPose();
    }
}