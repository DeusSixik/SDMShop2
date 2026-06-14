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

    protected boolean autoScale = false;

    protected float currentRenderScale = 1.0f;
    protected int renderX = 0;
    protected int renderY = 0;

    protected int paddingLeft = 0;
    protected int paddingRight = 0;
    protected int paddingTop = 0;
    protected int paddingBottom = 0;

    public SDMTextLabel(Component text) {
        this(0, 0, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
    }

    public SDMTextLabel(int x, int y, Component text) {
        this(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
    }

    public SDMTextLabel(Position selfPosition, Size size, Component text) {
        super(selfPosition, size);
        this.text = text;
        recalculateAutoScale();
    }

    public SDMTextLabel(int x, int y, int width, int height, Component text) {
        super(x, y, width, height);
        this.text = text;
        recalculateAutoScale();
    }

    public SDMTextLabel setPadding(int padding) {
        return setPadding(padding, padding, padding, padding);
    }

    public SDMTextLabel setPadding(int left, int right, int top, int bottom) {
        this.paddingLeft = left;
        this.paddingRight = right;
        this.paddingTop = top;
        this.paddingBottom = bottom;
        if (autoScale) {
            recalculateAutoScale();
        }
        return this;
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
            int newWidth = (int) (Minecraft.getInstance().font.width(text) * scale);
            int newHeight = (int) (Minecraft.getInstance().font.lineHeight * scale);
            this.setSize(newWidth, newHeight);
        } else {
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
            renderX = paddingLeft;
            renderY = paddingTop;
            return;
        }

        int textWidth = getTextWidth();
        int textHeight = getTextHeight();

        if (textWidth == 0 || textHeight == 0) return;

        float availableW = Math.max(0f, this.getSizeWidth() - paddingLeft - paddingRight);
        float availableH = Math.max(0f, this.getSizeHeight() - paddingTop - paddingBottom);

        float scaleX = availableW / textWidth;
        float scaleY = availableH / textHeight;

        currentRenderScale = Math.min(this.scale, Math.min(scaleX, scaleY));

        float scaledWidth = textWidth * currentRenderScale;
        float scaledHeight = textHeight * currentRenderScale;

        renderX = (int) (paddingLeft + (availableW - scaledWidth) * .5f);
        renderY = (int) (paddingTop + (availableH - scaledHeight) * .5f);
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

        poseStack.translate(position.getX() + renderX, position.getY() + renderY, 0);

        poseStack.scale(currentRenderScale, currentRenderScale, 1.0f);

        graphics.drawString(Minecraft.getInstance().font, text, 0, 0, color, false);

        poseStack.popPose();
    }

    public int getTextWidth() {
        return Minecraft.getInstance().font.width(this.text);
    }

    public int getTextHeight() {
        return Minecraft.getInstance().font.lineHeight;
    }
}