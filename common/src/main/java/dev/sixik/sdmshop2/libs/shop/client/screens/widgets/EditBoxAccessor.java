package dev.sixik.sdmshop2.libs.shop.client.screens.widgets;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.function.BiFunction;

public interface EditBoxAccessor {

    boolean isbEditable();
    int textColor();
    int textColorUneditable();
    int cursorPos();
    int displayPos();
    int highlightPos();
    Font font();
    String value();
    int frame();
    boolean bordered();
    BiFunction<String, Integer, FormattedCharSequence> formatter();
    Component hint();
    String suggestion();
    int maxLength();
    boolean isbBordered();
    void setHeight(int height);

}
