package dev.sixik.sdmshop2.libs.shop.commands;

import com.mojang.brigadier.CommandDispatcher;
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
        );
    }
}
