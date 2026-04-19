package dev.sixik.sdmshop2.libs.shop.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.sixik.sdmshop2.libs.shop.commands.builder.CommandBuilder;
import dev.sixik.sdmshop2.libs.shop.network.ShopNetworkManager;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SDMShopCommands {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection selection) {
        registerCommands(dispatcher);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {

        CommandBuilder.create("sdm_shop open_shop")
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
    }
}
