package dev.sixik.sdmshop2.libs.shop.client;

import com.lowdragmc.lowdraglib.client.shader.Shaders;
import com.lowdragmc.lowdraglib.client.shader.management.Shader;
import com.lowdragmc.lowdraglib.client.shader.management.ShaderProgram;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.utils.Rect;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.sixik.sdmshop2.SDMShop2;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.annotation.Nonnull;

@Environment(EnvType.CLIENT)
public class SDMShaders {

    public static ShaderProgram FILLED_FRAME_ROUND_BOX;
    public static Shader FILLED_FRAME_ROUND_BOX_F;

    public static void initShaderProgram() {
        FILLED_FRAME_ROUND_BOX = Util.make(new ShaderProgram(), program ->
                program.attach(FILLED_FRAME_ROUND_BOX_F).attach(Shaders.SCREEN_V));
    }

    public static void initShader() {
        FILLED_FRAME_ROUND_BOX_F = Shaders.load(Shader.ShaderType.FRAGMENT, new ResourceLocation(SDMShop2.MODID, "filled_frame_round_box"));
    }

    public static void drawFilledFrameRoundBox(@Nonnull GuiGraphics graphics, Rect square, float thickness, Vector4f radius, int fillColor, int borderColor) {
        FILLED_FRAME_ROUND_BOX.use(uniform -> {
            DrawerHelper.updateScreenVshUniform(graphics, uniform);
            uniform.glUniformMatrix4F("PoseStack", new Matrix4f());

            var point1 = new Vector4f(square.left - 0.25f, square.up - 0.25f, 0, 1);
            var point2 = new Vector4f(square.right - 0.25f, square.down - 0.25f, 0, 1);
            var matrix = graphics.pose().last().pose();
            point1.mul(matrix);
            point2.mul(matrix);

            var v1 = matrix.transform(new Vector4f(1, 1, 1, 1));
            var v2 = matrix.transform(new Vector4f(0, 0, 0, 1));
            var scale = v1.x - v2.x;

            uniform.glUniform4F("SquareVertex", point1.x, point1.y, point2.x, point2.y);
            uniform.glUniform4F("RoundRadius", radius.x() * scale, radius.y() * scale, radius.z() * scale, radius.w() * scale);
            uniform.glUniform1F("Thickness", thickness);

            // Передаем два цвета вместо одного
            uniform.fillRGBAColor("FillColor", fillColor);
            uniform.fillRGBAColor("BorderColor", borderColor);

            uniform.glUniform1F("Blur", 2);
        });

        RenderSystem.enableBlend();
        uploadScreenPosVertex();
    }

    private static void uploadScreenPosVertex() {
        var builder = Tesselator.getInstance().getBuilder();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        builder.vertex(-1.0, 1.0, 0.0).endVertex();
        builder.vertex(-1.0, -1.0, 0.0).endVertex();
        builder.vertex(1.0, -1.0, 0.0).endVertex();
        builder.vertex(1.0, 1.0, 0.0).endVertex();
        BufferUploader.draw(builder.end());
    }
}
