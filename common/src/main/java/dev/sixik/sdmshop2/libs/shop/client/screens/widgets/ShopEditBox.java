package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public class ShopEditBox extends EditBox {
    private final EditBoxAccessor accessor;
    private final TextFieldWidget textField;

    public ShopEditBox(Font font, int i, int j, int k, int l, Component component, TextFieldWidget textField) {
        super(font, i, j, k, l, component);
        this.accessor = (EditBoxAccessor) this;
        this.textField = textField;
    }

    public void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
        if (this.isVisible()) {
            int k;

//            if (accessor.isbBordered()) {
//                k = this.isFocused() ? -1 : -6250336;
//                guiGraphics.fill(this.getX() - 1, this.getY() - 1, this.getX() + this.width + 1, this.getY() + this.height + 1, k);
//                guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, -16777216);
//            }

            k = accessor.isbEditable() ? accessor.textColor() : accessor.textColorUneditable();
            int l = accessor.cursorPos() - accessor.displayPos();
            int m = accessor.highlightPos() - accessor.displayPos();
            String string = accessor.font().plainSubstrByWidth(accessor.value().substring(accessor.displayPos()), this.getInnerWidth());
            boolean bl = l >= 0 && l <= string.length();
            boolean bl2 = this.isFocused() && accessor.frame() / 6 % 2 == 0 && bl;
            int n = accessor.bordered() ? this.getX() + 4 : this.getX();
            int o = accessor.bordered() ? this.getY() + (this.height - 8) / 2 : this.getY();
            int p = n;
            if (m > string.length()) {
                m = string.length();
            }

            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, l) : string;
                p = guiGraphics.drawString(accessor.font(), accessor.formatter().apply(string2, accessor.displayPos()), p, o, k);
            }

            boolean bl3 = accessor.cursorPos() < accessor.value().length() || accessor.value().length() >= accessor.maxLength();
            int q = p;
            if (!bl) {
                q = l > 0 ? n + this.width : n;
            } else if (bl3) {
                --q;
                --p;
            }

            if (!string.isEmpty() && bl && l < string.length()) {
                guiGraphics.drawString(accessor.font(), accessor.formatter().apply(string.substring(l), accessor.cursorPos()), p, o, k);
            }

            if (accessor.hint() != null && string.isEmpty() && !this.isFocused()) {
                guiGraphics.drawString(accessor.font(), accessor.hint(), p, o, k);
            }

            if (!bl3 && accessor.suggestion() != null) {
                guiGraphics.drawString(accessor.font(), accessor.suggestion(), q - 1, o, -8355712);
            }

            int var10003;
            int var10004;
            int var10005;
            if (bl2) {
                if (bl3) {
                    RenderType var10001 = RenderType.guiOverlay();
                    var10003 = o - 1;
                    var10004 = q + 1;
                    var10005 = o + 1;
                    Objects.requireNonNull(accessor.font());
                    guiGraphics.fill(var10001, q, var10003, var10004, var10005 + 9, -3092272);
                } else {
                    guiGraphics.drawString(accessor.font(), "_", q, o, k);
                }
            }

            if (m != l) {
                int r = n + accessor.font().width(string.substring(0, m));
                var10003 = o - 1;
                var10004 = r - 1;
                var10005 = o + 1;
                Objects.requireNonNull(accessor.font());
                this.renderHighlight(guiGraphics, q, var10003, var10004, var10005 + 9);
            }

        }
    }

    private void renderHighlight(GuiGraphics guiGraphics, int i, int j, int k, int l) {
        int m;
        if (i < k) {
            m = i;
            i = k;
            k = m;
        }

        if (j < l) {
            m = j;
            j = l;
            l = m;
        }

        if (k > this.getX() + this.width) {
            k = this.getX() + this.width;
        }

        if (i > this.getX() + this.width) {
            i = this.getX() + this.width;
        }

        guiGraphics.fill(RenderType.guiTextHighlight(), i, j, k, l, -16776961);
    }
}
