package dev.sixik.sdmshop2.libs.shop.commands.builder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class CommandBuilder {

    @FunctionalInterface
    public interface CommandAction {
        void execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException;
    }

    private final String[] path;
    private Predicate<CommandSourceStack> requirement = s -> true;
    private Command<CommandSourceStack> execution;
    private final List<ArgumentBuilder<CommandSourceStack, ?>> children = new ArrayList<>();

    private CommandBuilder(String route) {
        this.path = route.startsWith("/") ? route.substring(1).split(" ") : route.split(" ");
    }

    public static CommandBuilder create(String route) {
        return new CommandBuilder(route);
    }

    public CommandBuilder requires(int permissionLevel) {
        this.requirement = s -> s.hasPermission(permissionLevel);
        return this;
    }

    public CommandBuilder requires(Predicate<CommandSourceStack> requirement) {
        this.requirement = requirement;
        return this;
    }

    public CommandBuilder executes(Command<CommandSourceStack> command) {
        this.execution = command;
        return this;
    }

    public CommandBuilder executesVoid(CommandAction action) {
        this.execution = ctx -> {
            action.execute(ctx);
            return Command.SINGLE_SUCCESS;
        };
        return this;
    }

    public CommandBuilder then(ArgumentBuilder<CommandSourceStack, ?> child) {
        this.children.add(child);
        return this;
    }

    public void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (path.length == 0) return;

        LiteralArgumentBuilder<CommandSourceStack> leaf = Commands.literal(path[path.length - 1]);

        leaf.requires(this.requirement);
        if (this.execution != null) {
            leaf.executes(this.execution);
        }
        for (ArgumentBuilder<CommandSourceStack, ?> child : children) {
            leaf.then(child);
        }

        LiteralArgumentBuilder<CommandSourceStack> currentNode = leaf;
        for (int i = path.length - 2; i >= 0; i--) {
            LiteralArgumentBuilder<CommandSourceStack> parent = Commands.literal(path[i]);
            parent.then(currentNode);
            currentNode = parent;
        }

        dispatcher.register(currentNode);
    }
}