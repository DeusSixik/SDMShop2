package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ShopTextField extends TextFieldWidget {

    public ShopTextField() {
        super();
        if (textField != null){
            Font fontRenderer = Minecraft.getInstance().font;
            this.textField = new ShopEditBox(fontRenderer, 0, 0, 60, 15, Component.literal("text field"), this);
            this.textField.setBordered(true);
            isBordered = true;
            this.textField.setMaxLength(this.maxStringLength);
            this.textField.setResponder(this::onTextChanged);
        }
    }

    public ShopTextField(int xPosition, int yPosition, int width, int height, Supplier<String> textSupplier, Consumer<String> textResponder) {
        super(xPosition, yPosition, width, height, textSupplier, textResponder);
        if (textField != null){
            Font fontRenderer = Minecraft.getInstance().font;
            this.textField = new ShopEditBox(fontRenderer, xPosition, yPosition, width, height, Component.literal("text field"), this);
            this.textField.setBordered(true);
            isBordered = true;
            this.textField.setMaxLength(this.maxStringLength);
            this.textField.setResponder(this::onTextChanged);
        }
    }
}
