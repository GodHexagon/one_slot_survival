package com.github.godhexagon.oneslotsurvival.world.command.admin;

import com.github.godhexagon.oneslotsurvival.rule.level.Level;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils.getDefaultPlayers;

import java.util.Collection;

/**
 * ロール管理コマンド
 * /oneslot admin role サブコマンド群
 */
public class RoleCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("role")
            .then(Commands.literal("get")
                .executes(context -> getRoleStatus(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> getRoleStatus(context, EntityArgument.getPlayers(context, "players")))))
            .then(Commands.literal("set")
                .then(Commands.argument("roleName", StringArgumentType.word())
                    .executes(context -> setRole(context, getDefaultPlayers(context), true))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> setRole(context, EntityArgument.getPlayers(context, "players"), true)))))
            .then(Commands.literal("setNoClear")
                .then(Commands.argument("roleName", StringArgumentType.word())
                    .executes(context -> setRole(context, getDefaultPlayers(context), false))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> setRole(context, EntityArgument.getPlayers(context, "players"), false)))))
            .then(Commands.literal("clear")
                .executes(context -> clearRole(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> clearRole(context, EntityArgument.getPlayers(context, "players")))));
    }

    private static int getRoleStatus(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                MainRole role = RoleManager.getRole(player);
                String roleCommandName = role.getCommandName();
                Component roleDisplayName = role.getDisplayName();

                context.getSource().sendSuccess(
                    () -> Component.literal("Role for " + player.getName().getString() + ": " + roleCommandName + " (")
                        .append(roleDisplayName)
                        .append(")"),
                    false
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setRole(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean resetProgress) {
        try {
            String roleName = StringArgumentType.getString(context, "roleName");
            MainRole role = MainRole.fromCommandName(roleName);

            if (role == null) {
                context.getSource().sendFailure(
                    Component.literal("Invalid role name: " + roleName + ". Valid roles: unassigned, miner, warrior")
                );
                return 0;
            }

            for (ServerPlayer player : players) {
                RoleManager.setRole(player, role);

                // setNoClearでないとき
                if (resetProgress) {
                    Level.reset(player);
                }

                String roleCommandName = role.getCommandName();
                Component roleDisplayName = role.getDisplayName();

                context.getSource().sendSuccess(
                    () -> Component.literal("Set role to " + roleCommandName + " (")
                        .append(roleDisplayName)
                        .append(") for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your role has been set to " + roleCommandName + " (")
                        .append(roleDisplayName)
                        .append(") by " + context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearRole(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                RoleManager.clearRole(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared role for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your role has been cleared by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
