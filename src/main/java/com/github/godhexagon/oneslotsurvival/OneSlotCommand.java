package com.github.godhexagon.oneslotsurvival;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * Command handling for One Slot Survival mod.
 * Provides /oneslot command with enable/disable/toggle/status subcommands.
 */
public class OneSlotCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("oneslot")
                .requires(source -> source.hasPermission(2)) // OP level 2 required
                .then(Commands.argument("players", EntityArgument.players())
                    .then(Commands.literal("enable")
                        .executes(context -> setOneSlotMode(context, true)))
                    .then(Commands.literal("disable")
                        .executes(context -> setOneSlotMode(context, false)))
                    .then(Commands.literal("toggle")
                        .executes(context -> toggleOneSlotMode(context)))
                    .then(Commands.literal("status")
                        .executes(context -> getOneSlotStatus(context))))
                .then(Commands.literal("status")
                    .executes(context -> getOwnStatus(context)))
        );
    }

    private static int setOneSlotMode(CommandContext<CommandSourceStack> context, boolean enabled) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        String action = enabled ? "enabled" : "disabled";

        for (ServerPlayer player : players) {
            OneSlotManager.setEnabled(player, enabled);
            context.getSource().sendSuccess(
                () -> Component.literal("One Slot Survival " + action + " for " + player.getName().getString()),
                true
            );

            // Notify the target player
            player.sendSystemMessage(
                Component.literal("One Slot Survival has been " + action + " for you by " +
                    context.getSource().getDisplayName().getString())
            );
        }

        return players.size();
    }

    private static int toggleOneSlotMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

        for (ServerPlayer player : players) {
            boolean newState = OneSlotManager.toggle(player);
            String action = newState ? "enabled" : "disabled";

            context.getSource().sendSuccess(
                () -> Component.literal("One Slot Survival " + action + " for " + player.getName().getString()),
                true
            );

            // Notify the target player
            player.sendSystemMessage(
                Component.literal("One Slot Survival has been " + action + " for you by " +
                    context.getSource().getDisplayName().getString())
            );
        }

        return players.size();
    }

    private static int getOneSlotStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");

        for (ServerPlayer player : players) {
            boolean enabled = OneSlotManager.isEnabled(player);
            String status = enabled ? "enabled" : "disabled";
            context.getSource().sendSuccess(
                () -> Component.literal("One Slot Survival is " + status + " for " + player.getName().getString()),
                false
            );
        }

        return players.size();
    }

    private static int getOwnStatus(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            context.getSource().sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }

        boolean enabled = OneSlotManager.isEnabled(player);
        String status = enabled ? "enabled" : "disabled";
        context.getSource().sendSuccess(
            () -> Component.literal("One Slot Survival is " + status + " for you"),
            false
        );

        return 1;
    }
}