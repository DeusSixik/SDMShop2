package dev.sixik.sdmshop2.libs.shop.commands;

import com.mojang.brigadier.CommandDispatcher;
import dev.sixik.sdmshop2.SDMShop2;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.commands.builder.CommandBuilder;
import dev.sixik.sdmshop2.libs.shop.components.CommandRewardComponent;
import dev.sixik.sdmshop2.libs.shop.components.api.ConditionComponent;
import dev.sixik.sdmshop2.libs.shop.network.ShopNetworkManager;
import dev.sixik.sdmshop2.libs.shop.scripting.ScriptConditionComponent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
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
        debugShop.setShouldSave(false);
        shopOffer = ShopOffer.create(UUID.randomUUID(), true);
        shopOffer.addComponent(new ScriptConditionComponent("test_script"));

        debugShop.getEntries().addEntry(shopOffer);
        debugShop.getCategories().reindex();

        ShopTable.Instance.addShop(debugShop);
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

                    final var player = ctx.getSource().getPlayer();

                    ShopNetworkManager.fetchServerCondition(shopOffer).whenComplete(((data, throwable) -> {
                        if(throwable != null) {
                            SDMShop2.LOGGER.error(throwable.getMessage(), throwable);
                            return;
                        }

                        for (Map.Entry<ConditionComponent, Boolean> entry :
                                data.entrySet()) {
                            player.sendSystemMessage(Component.literal(entry.getKey().toString() + " : " + entry.getValue()));
                        }
                    }));

//                    final var component = shopOffer.addComponent(new CommandRewardComponent());
//                    ShopNetworkManager.sendNewComponent(debugShop, shopOffer, component, ctx.getSource().getPlayerOrException());
                })
                .register(dispatcher);
    }
}
