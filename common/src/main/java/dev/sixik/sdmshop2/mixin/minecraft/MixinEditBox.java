package dev.sixik.sdmshop2.mixin.minecraft;

import dev.sixik.sdmshop2.libs.shop.client.screens.widgets.EditBoxAccessor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiFunction;

@Mixin(EditBox.class)
public abstract class MixinEditBox extends AbstractWidget implements EditBoxAccessor {

    @Shadow private boolean isEditable;

    @Shadow private int textColor;

    @Shadow private int textColorUneditable;

    @Shadow private int cursorPos;

    @Shadow private int displayPos;

    @Shadow private int highlightPos;

    @Shadow @Final private Font font;

    @Shadow private String value;

    @Shadow private int frame;

    @Shadow private boolean bordered;

    @Shadow private BiFunction<String, Integer, FormattedCharSequence> formatter;

    @Shadow @Nullable private Component hint;

    @Shadow @Nullable private String suggestion;

    @Shadow private int maxLength;

    public MixinEditBox(int i, int j, int k, int l, Component component) {
        super(i, j, k, l, component);
    }

    @Shadow protected abstract boolean isBordered();

    @Override
    public boolean isbEditable() {
        return isEditable;
    }

    @Override
    public int textColor() {
        return textColor;
    }

    @Override
    public int textColorUneditable() {
        return textColorUneditable;
    }

    @Override
    public int cursorPos() {
        return cursorPos;
    }

    @Override
    public int displayPos() {
        return displayPos;
    }

    @Override
    public int highlightPos() {
        return highlightPos;
    }

    @Override
    public Font font() {
        return font;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public int frame() {
        return frame;
    }

    @Override
    public boolean bordered() {
        return bordered;
    }

    @Override
    public BiFunction<String, Integer, FormattedCharSequence> formatter() {
        return formatter;
    }

    @Override
    public Component hint() {
        return hint;
    }

    @Override
    public String suggestion() {
        return suggestion;
    }

    @Override
    public int maxLength() {
        return maxLength;
    }

    @Override
    public boolean isbBordered() {
        return bordered;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }


}
