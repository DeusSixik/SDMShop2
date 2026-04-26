package dev.sixik.sdmshop2.mixin.ldlib;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.sixik.sdmshop2.libs.shop.client.WidgetGroupAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = WidgetGroup.class, remap = false)
public abstract class MixinWidgetGroup$access implements WidgetGroupAccessor {
    @Shadow
    protected abstract void onChildSizeUpdate(Widget child);

    @Override
    public void sdm$onChildSizeUpdate(Widget child) {
        onChildSizeUpdate(child);
    }
}
