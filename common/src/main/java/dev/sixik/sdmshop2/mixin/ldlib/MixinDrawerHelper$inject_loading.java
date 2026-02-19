package dev.sixik.sdmshop2.mixin.ldlib;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import dev.sixik.sdmshop2.libs.shop.client.SDMShaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DrawerHelper.class, remap = false)
public class MixinDrawerHelper$inject_loading {

    @Inject(method = "init", at = @At("RETURN"))
    private static void bts$init(CallbackInfo ci) {
        SDMShaders.initShaderProgram();
    }
}
