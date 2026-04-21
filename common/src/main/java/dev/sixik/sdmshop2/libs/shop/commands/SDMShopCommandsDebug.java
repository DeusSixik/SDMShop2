package dev.sixik.sdmshop2.libs.shop.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.commands.builder.CommandBuilder;
import dev.sixik.sdmshop2.libs.shop.components.CommandRewardComponent;
import dev.sixik.sdmshop2.libs.shop.network.ShopNetworkManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class SDMShopCommandsDebug {

    private static ShopInstance debugShop = create();
    private static ShopOffer shopOffer;

    private static ShopInstance create() {
        ShopInstance debugShop = ShopInstance.createManager(new ResourceLocation("sdm", "test"), true);;
        shopOffer = ShopOffer.create(UUID.randomUUID(), true);
        debugShop.getEntries().addEntry(shopOffer);
        debugShop.getCategories().reindex();
        return debugShop;
    }

    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandBuilder.create("sdm_shop tests send_new_shop")
                .requires(2)
                .executesVoid(ctx -> {
                    ShopNetworkManager.sendShopData(debugShop, ctx.getSource().getPlayerOrException());
                })
                .register(dispatcher);
        CommandBuilder.create("sdm_shop tests send_new_component")
                .requires(2)
                .executesVoid(ctx -> {
                    final var component = shopOffer.addComponent(new CommandRewardComponent());
                    ShopNetworkManager.sendNewComponent(debugShop, shopOffer, component, ctx.getSource().getPlayerOrException());
                })
                .register(dispatcher);
    }
}
