package com.github.godhexagon.oneslotsurvival.world.command;

import com.github.godhexagon.oneslotsurvival.world.util.player.PlayerModValidity;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

import static com.github.godhexagon.oneslotsurvival.world.command.CommandUtils.getDefaultPlayers;

/**
 * One Slot Survival の有効/無効管理コマンド
 * /oneslot admin validity サブコマンド群
 */
public class ValidityCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("validity")
            .then(Commands.literal("get")
                .executes(context -> getOneSlotStatus(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> getOneSlotStatus(context, EntityArgument.getPlayers(context, "players")))))
            .then(Commands.literal("enable")
                .executes(context -> setOneSlotMode(context, getDefaultPlayers(context), true))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> setOneSlotMode(context, EntityArgument.getPlayers(context, "players"), true))))
            .then(Commands.literal("disable")
                .executes(context -> setOneSlotMode(context, getDefaultPlayers(context), false))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> setOneSlotMode(context, EntityArgument.getPlayers(context, "players"), false))))
            .then(Commands.literal("toggle")
                .executes(context -> toggleOneSlotMode(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> toggleOneSlotMode(context, EntityArgument.getPlayers(context, "players")))));
    }

    private static int setOneSlotMode(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean enabled) {
        try {
            String action = enabled ? "enabled" : "disabled";

            for (ServerPlayer player : players) {
                PlayerModValidity.setEnabled(player, enabled);
                context.getSource().sendSuccess(
                    () -> Component.literal("One Slot Survival " + action + " for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("One Slot Survival has been " + action + " for you by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int toggleOneSlotMode(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                boolean newState = PlayerModValidity.toggle(player);
                String action = newState ? "enabled" : "disabled";

                context.getSource().sendSuccess(
                    () -> Component.literal("One Slot Survival " + action + " for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("One Slot Survival has been " + action + " for you by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int getOneSlotStatus(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                boolean enabled = PlayerModValidity.isEnabled(player);
                String status = enabled ? "enabled" : "disabled";
                context.getSource().sendSuccess(
                    () -> Component.literal("One Slot Survival is " + status + " for " + player.getName().getString()),
                    false
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
