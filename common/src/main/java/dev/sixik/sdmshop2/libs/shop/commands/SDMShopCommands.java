package dev.sixik.sdmshop2.libs.shop.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import dev.sixik.sdmshop2.libs.shop.network.ShopNetworkManager;
import dev.sixik.sdmshop2.tests.economy.EconomyTest;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class SDMShopCommands {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        registerCommands(commandSourceStackCommandDispatcher);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("sdm_shop")
                        .then(Commands.literal("open_shop")
                        )
                        .then(Commands.literal("test")
                                .executes(s -> {
                                    EconomyTest.commandTest(s.getSource().getPlayerOrException());
                                    return 0;
                                })
                        )
                        .then(Commands.literal("synchronization").requires(s -> s.hasPermission(2)).then(synchronizationCommands()))
        );
    }

    private static ArgumentBuilder<CommandSourceStack, ?> synchronizationCommands() {
        return Commands.literal("limiter_data").executes(commandContext -> {
            ShopNetworkManager.sendLimiterData(commandContext.getSource().getPlayerOrException());
            return 0;
        });
    }
}
