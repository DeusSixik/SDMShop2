package dev.sixik.sdmshop2.mixin;

import dev.sixik.sdmshop2.libs.platform.SDMPlatform;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyCurrencyRegistry;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import net.minecraft.commands.Commands;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ReloadableServerResources.class)
public class MixinReloadableServerResources {

    @Inject(method = "loadResources", at = @At("RETURN"))
    private static void bts$loadResources(ResourceManager resourceManager, RegistryAccess.Frozen frozen, FeatureFlagSet featureFlagSet, Commands.CommandSelection commandSelection, int i, Executor executor, Executor executor2, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        cir.getReturnValue().thenRun(SDMPlatform::onReload);
    }
}
