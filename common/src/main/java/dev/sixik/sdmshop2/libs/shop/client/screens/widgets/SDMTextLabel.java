package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

public class SDMTextLabel extends Widget {

    @Getter
    private Component text;

    @Getter
    private float scale = 1;

    public int color = 0xFFFFFFFF;

    public SDMTextLabel(Component text) {
        this(0, 0, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
    }

    public SDMTextLabel(int x, int y, Component text) {
        this(x, y, Minecraft.getInstance().font.width(text), Minecraft.getInstance().font.lineHeight, text);
    }

    public SDMTextLabel(Position selfPosition, Size size, Component text) {
        super(selfPosition, size);
        this.text = text;
    }

    public SDMTextLabel(int x, int y, int width, int height, Component text) {
        super(x, y, width, height);
        this.text = text;
    }

    public void setText(Component text) {
        this.text = text;
        int newWidth = Minecraft.getInstance().font.width(text);
        int newHeight = Minecraft.getInstance().font.lineHeight;
        this.setSize(newWidth, newHeight);
    }

    public void setScale(float scale) {
        this.scale = scale;
        int newWidth = (int) (Minecraft.getInstance().font.width(text) * scale);
        int newHeight = (int) (Minecraft.getInstance().font.lineHeight * scale);
        this.setSize(newWidth, newHeight);
    }

    @Override
    public void drawInBackground(@NonNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        var position = getPosition();

        if (this.scale == 1.0f) {
            graphics.drawString(Minecraft.getInstance().font, text, position.getX(), position.getY(), color);
            return;
        }

        PoseStack poseStack = graphics.pose();

        poseStack.pushPose();
        poseStack.translate(position.getX(), position.getY(), 0);
        poseStack.scale(scale, scale, 1.0f);
        graphics.drawString(Minecraft.getInstance().font, text, 0, 0, color, false);
        poseStack.popPose();
    }
}