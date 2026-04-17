package dev.sixik.sdmshop2.libs.shop.client.textures;

import com.lowdragmc.lowdraglib.gui.texture.TransformTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import net.minecraft.client.gui.GuiGraphics;

public class GradientTexture extends TransformTexture {

    private int color1;
    private int color2;
    private boolean vec;

    public GradientTexture(int color1, int color2,boolean vec){
        this.color1 = color1;
        this.color2 = color2;
        this.vec = vec;
    }

    @Override
    protected void drawInternal(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        DrawerHelper.drawGradientRect(graphics, (int)x, (int)y, width, height, color1, color2,vec);
    }


}
