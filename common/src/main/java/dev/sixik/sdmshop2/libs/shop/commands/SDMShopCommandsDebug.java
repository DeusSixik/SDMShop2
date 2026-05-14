package dev.sixik.sdmshop2.libs.shop.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.client.config.ComponentConfigurationGroup;
import dev.sixik.sdmshop2.libs.shop.client.screens.ShopScreenManager;
import dev.sixik.sdmshop2.libs.shop.commands.builder.CommandBuilder;
import dev.sixik.sdmshop2.libs.shop.components.limiter.LimiterComponent;
import dev.sixik.sdmshop2.libs.shop.generator.DefaultShopGenerator;
import dev.sixik.sdmshop2.libs.shop.network.ShopNetworkManager;
import dev.sixik.sdmshop2.libs.shop.scripting.ScriptConditionComponent;
import dev.sixik.sdmshop2.utils.ShopUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public class SDMShopCommandsDebug {

    private static ShopInstance debugShop;

    private static ShopInstance getShop() {
        if(debugShop == null) {
            debugShop = create();
        }
        return debugShop;
    }

    private static ShopOffer shopOffer;

    private static ShopInstance create() {
        ShopInstance debugShop = ShopInstance.createManager(new ResourceLocation("sdm", "test"), true);;
        shopOffer = ShopOffer.create(UUID.randomUUID(), true);
        shopOffer.addComponent(new ScriptConditionComponent("test_script"));
        shopOffer.addComponent(new LimiterComponent(LimiterComponent.LimiterType.World, 1));

        debugShop.getEntries().addEntry(shopOffer);
        debugShop.getCategories().reindex();

        ShopTable.Instance.addShop(debugShop);
        ShopTable.Instance.save(debugShop);
        return debugShop;
    }

    public static void init(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandBuilder.create("sdm_shop tests send_new_shop")
                .requires(2)
                .executesVoid(ctx -> {
                    ShopNetworkManager.sendShopData(getShop(), ctx.getSource().getPlayerOrException());
                })
                .register(dispatcher);
        CommandBuilder.create("sdm_shop tests send_new_component")
                .requires(2)
                .executesVoid(ctx -> {
//                    ShopLimiterTableServer.getInstance().getOfferDatga(shopOffer.getUUID()).set(50);

//                    ShopNetworkManager.requestShopAndOpen(new ResourceLocation("sdm", "test"));

//                    final var component = shopOffer.addComponent(new CommandRewardComponent());
//                    ShopNetworkManager.sendNewComponent(debugShop, shopOffer, component, ctx.getSource().getPlayerOrException());
                })
                .register(dispatcher);
        CommandBuilder.create("sdm_shop tests generate_default")
                .requires(2)
                .executesVoid((ctx) -> {
                    DefaultShopGenerator.registerDefault();
                })
                .register(dispatcher);
        CommandBuilder.create("sdm_shop tests config")
                .requires(2)
                .executesVoid((ctx) -> {
                    ShopUtils.openWidget(new ComponentConfigurationGroup());
                })
                .register(dispatcher);
    }
}
