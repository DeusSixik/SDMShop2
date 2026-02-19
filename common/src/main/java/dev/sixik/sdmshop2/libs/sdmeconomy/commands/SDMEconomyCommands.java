package dev.sixik.sdmshop2.libs.sdmeconomy.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.sixik.sdmshop2.libs.sdmeconomy.BankAccount;
import dev.sixik.sdmshop2.libs.sdmeconomy.DynamicStoredCurrency;
import dev.sixik.sdmshop2.libs.sdmeconomy.SDMEconomyService;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SDMEconomyCommands {

    public static final DynamicStoredCurrency DYNAMIC_CURRENCY =
            new DynamicStoredCurrency(ResourceLocation.tryBuild("sdm", "coin"));

    public static void registerCommands(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        registerCommands(commandSourceStackCommandDispatcher);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("sdm_economy")
                .then(Commands.literal("balance")
                    .then(Commands.argument("money", ResourceLocationArgument.id())
                        .suggests(SDMEconomyCommands::moneyIds)
                        .executes(SDMEconomyCommands::balanceCommand)
                        .then(Commands.argument("player", EntityArgument.player())
                            .requires(source -> source.hasPermission(2))
                            .executes(SDMEconomyCommands::balanceTargetCommand)
                        )
                    )
                )
                .then(Commands.literal("create_money")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("money", ResourceLocationArgument.id())
                        .executes(SDMEconomyCommands::createMoney)
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(SDMEconomyCommands::createMoneyTarget)
                        )
                    )
                )
                .then(Commands.literal("remove_money")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("money", ResourceLocationArgument.id())
                        .suggests(SDMEconomyCommands::moneyIds)
                        .executes(SDMEconomyCommands::removeMoney)
                        .then(Commands.argument("target", EntityArgument.player())
                            .executes(SDMEconomyCommands::removeMoneyTarget)
                        )
                    )
                )
                .then(Commands.literal("pay")
                    .then(Commands.argument("money", ResourceLocationArgument.id()).suggests(SDMEconomyCommands::moneyIds)
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01)) // Минимум 0.01
                                .executes(SDMEconomyCommands::payCommand)
                            )
                        )
                    )
                )
                .then(Commands.literal("set")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("money", ResourceLocationArgument.id()).suggests(SDMEconomyCommands::moneyIds)
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0))
                                .executes(SDMEconomyCommands::setMoneyCommand)
                            )
                        )
                    )
                )
                .then(Commands.literal("add")
                    .requires(source -> source.hasPermission(2))
                    .then(Commands.argument("money", ResourceLocationArgument.id()).suggests(SDMEconomyCommands::moneyIds)
                        .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.01))
                                .executes(SDMEconomyCommands::addMoneyCommand)
                            )
                        )
                    )
                )
        );
    }

    private static CompletableFuture<Suggestions> moneyIds(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        final ServerPlayer player = context.getSource().getPlayer();

        List<String> moneys = new ArrayList<>();
        if(player != null) {
            final BankAccount account = SDMEconomyService.getInstance().getAccount(player.getGameProfile().getId());
            moneys.addAll(account.getCurrenciesIds().stream().map(ResourceLocation::toString).toList());
        }

        return SharedSuggestionProvider.suggest(moneys.stream(), builder);
    }

    private static int balanceCommand(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;

        return showBalance(context.getSource(), player, ResourceLocationArgument.getId(context, "money"));
    }

    private static int balanceTargetCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");

        return showBalance(context.getSource(), targetPlayer, ResourceLocationArgument.getId(context, "money"));
    }

    private static int showBalance(CommandSourceStack source, ServerPlayer target, ResourceLocation money) {
        DYNAMIC_CURRENCY.setId(money);

        BankAccount account = SDMEconomyService.getInstance().getAccount(target.getGameProfile().getId());
        Component outMessage = Component.literal("Balance '").append(money.toString()).append("': ")
                .append(String.valueOf(account.getBalance(DYNAMIC_CURRENCY).doubleValue()));

        source.sendSuccess(() -> outMessage, false);
        return 1;
    }


    private static int createMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return processCreateMoney(context.getSource(), player, ResourceLocationArgument.getId(context, "money"));
    }

    private static int createMoneyTarget(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        return processCreateMoney(context.getSource(), target, ResourceLocationArgument.getId(context, "money"));
    }

    private static int processCreateMoney(CommandSourceStack source, ServerPlayer target, ResourceLocation money) {
        DYNAMIC_CURRENCY.setId(money);

        final BankAccount account = SDMEconomyService.getInstance().getAccount(target.getGameProfile().getId());
        account.setBalance(DYNAMIC_CURRENCY, BigDecimal.ZERO);

        source.sendSuccess(() -> Component.literal("Created currency '" + money + "' for " + target.getScoreboardName()), true);
        return 1;
    }

    private static int removeMoney(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        return processRemoveMoney(context.getSource(), player, ResourceLocationArgument.getId(context, "money"));
    }

    private static int removeMoneyTarget(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(context, "target");
        return processRemoveMoney(context.getSource(), target, ResourceLocationArgument.getId(context, "money"));
    }

    private static int processRemoveMoney(CommandSourceStack source, ServerPlayer target, ResourceLocation money) {
        DYNAMIC_CURRENCY.setId(money);

        final BankAccount account = SDMEconomyService.getInstance().getAccount(target.getGameProfile().getId());
        account.removeBalance(DYNAMIC_CURRENCY);

        source.sendSuccess(() -> Component.literal("Removed currency '" + money + "' from " + target.getScoreboardName()), true);
        return 1;
    }

    private static int payCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer sourcePlayer = context.getSource().getPlayerOrException();
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");

        if (sourcePlayer.equals(targetPlayer)) {
            context.getSource().sendFailure(Component.literal("You cannot pay yourself!"));
            return 0;
        }

        final var moneyId = ResourceLocationArgument.getId(context, "money");
        double amount = DoubleArgumentType.getDouble(context, "amount");
        DYNAMIC_CURRENCY.setId(moneyId);

        SDMEconomyService service = SDMEconomyService.getInstance();
        BankAccount sourceAccount = service.getAccount(sourcePlayer.getGameProfile().getId());
        BankAccount targetAccount = service.getAccount(targetPlayer.getGameProfile().getId());

        if (sourceAccount.getBalance(DYNAMIC_CURRENCY).doubleValue() < amount) {
            context.getSource().sendFailure(Component.literal("Insufficient funds!"));
            return 0;
        }

        sourceAccount.modify(DYNAMIC_CURRENCY, BigDecimal.valueOf(-amount));  // Списываем
        targetAccount.modify(DYNAMIC_CURRENCY, BigDecimal.valueOf(amount));   // Начисляем

        context.getSource().sendSuccess(() -> Component.literal("Successfully paid " + amount + " '" + moneyId + "' to " + targetPlayer.getScoreboardName()), false);
        targetPlayer.sendSystemMessage(Component.literal("You received " + amount + " '" + moneyId + "' from " + sourcePlayer.getScoreboardName()));
        return 1;
    }

    private static int setMoneyCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");
        final ResourceLocation moneyId = ResourceLocationArgument.getId(context, "money");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        DYNAMIC_CURRENCY.setId(moneyId);
        BankAccount targetAccount = SDMEconomyService.getInstance().getAccount(targetPlayer.getGameProfile().getId());

        targetAccount.setBalance(DYNAMIC_CURRENCY, BigDecimal.valueOf(amount));

        context.getSource().sendSuccess(() -> Component.literal("Set balance of " + targetPlayer.getScoreboardName() + " to " + amount + " '" + moneyId + "'"), true);
        return 1;
    }

    private static int addMoneyCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target");
        final var moneyId = ResourceLocationArgument.getId(context, "money");
        double amount = DoubleArgumentType.getDouble(context, "amount");

        DYNAMIC_CURRENCY.setId(moneyId);
        BankAccount targetAccount = SDMEconomyService.getInstance().getAccount(targetPlayer.getGameProfile().getId());

        targetAccount.modify(DYNAMIC_CURRENCY, BigDecimal.valueOf(amount));

        context.getSource().sendSuccess(() -> Component.literal("Added " + amount + " '" + moneyId + "' to " + targetPlayer.getScoreboardName()), true);
        return 1;
    }

}
