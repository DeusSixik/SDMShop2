package dev.sixik.sdmshop2.libs.shop.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.architectury.platform.Platform;
import dev.sixik.sdmshop2.libs.shop.SDMShopPlatform;
import dev.sixik.sdmshop2.libs.shop.base.ShopEntity;
import dev.sixik.sdmshop2.libs.shop.base.ShopInstance;
import dev.sixik.sdmshop2.libs.shop.base.ShopOffer;
import dev.sixik.sdmshop2.libs.shop.base.ShopTable;
import dev.sixik.sdmshop2.libs.shop.client.SDMShopClient;
import dev.sixik.sdmshop2.libs.shop.client.SDMShopClientEvents;
import dev.sixik.sdmshop2.libs.shop.commands.builder.CommandBuilder;
import dev.sixik.sdmshop2.libs.shop.components.CommandRewardComponent;
import dev.sixik.sdmshop2.libs.shop.network.ShopNetworkManager;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SDMShopCommands {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        registerCommands(dispatcher);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {

        CommandBuilder.create("sdm_shop create_shop")
                .requires(2)
                .then(Commands.argument("shop_id", ResourceLocationArgument.id())
                        .executes(ctx -> {
                            ResourceLocation shopId = ResourceLocationArgument.getId(ctx, "shop_id");
                            if(shopId.getNamespace().equals("minecraft"))
                                shopId = new ResourceLocation("sdm", shopId.getPath());

                            ResourceLocation finalShopId = shopId;

                            final ShopInstance manager = ShopInstance.createManager(shopId, true);
                            ShopTable.Instance.addShop(manager);
                            ShopTable.Instance.save(manager);
                            ctx.getSource().sendSuccess(
                                    () -> Component.literal("Shop created: " + finalShopId),
                                    true
                            );

                            return 1;
                        })
                )
                .register(dispatcher);

        CommandBuilder.create("sdm_shop open_shop")
                .requires(2)
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("shop_id", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    return SharedSuggestionProvider.suggestResource(ShopTable.Instance.getShopsId(), builder);
                                })

                                .executes(ctx -> {
                                    final Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "targets");
                                    ResourceLocation shopId = ResourceLocationArgument.getId(ctx, "shop_id");
                                    if(shopId.getNamespace().equals("minecraft"))
                                        shopId = new ResourceLocation("sdm", shopId.getPath());

                                    final ShopInstance shop = ShopTable.Instance.getShop(shopId);
                                    if(shop == null) {
                                        ctx.getSource().sendFailure(Component.literal("Shop with id '" + shopId + "' not found").withStyle(ChatFormatting.RED));
                                        return 0;
                                    }

                                    for (ServerPlayer target : targets) {
                                        ShopNetworkManager.sendShopData(shop, target);
                                    }
                                    return 1;
                                })
                        )
                )
                .register(dispatcher);

        CommandBuilder.create("sdm_shop test")
                .executesVoid(ctx -> {
                    EconomyTest.commandTest(ctx.getSource().getPlayerOrException());
                })
                .register(dispatcher);

        CommandBuilder.create("sdm_shop synchronization limiter_data")
                .requires(2)
                .executesVoid(ctx -> {
                    ShopNetworkManager.sendLimiterData(ctx.getSource().getPlayerOrException());
                })
                .register(dispatcher);

        CommandBuilder.create("sdm_shop reload shops")
                .requires(2)
                .executesVoid(ctx -> {

                    if(ShopTable.Instance.isReloading()) {
                        ctx.getSource().sendFailure(Component.literal("Shops are already reloading").withStyle(ChatFormatting.RED));
                        return;
                    }

                    ShopTable.Instance.reload();
                    ctx.getSource().sendSuccess(() -> Component.literal("Shops reloaded successfully").withStyle(ChatFormatting.GREEN), true);
                })
                .register(dispatcher);

        if(Platform.isDevelopmentEnvironment())
            SDMShopCommandsDebug.init(dispatcher);
    }
}
