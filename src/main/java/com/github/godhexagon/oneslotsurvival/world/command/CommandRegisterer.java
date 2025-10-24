package com.github.godhexagon.oneslotsurvival.world.command;

import com.github.godhexagon.oneslotsurvival.world.util.player.PlayerModValidity;
import com.github.godhexagon.oneslotsurvival.world.util.role.MainRole;
import com.github.godhexagon.oneslotsurvival.world.util.role.RoleManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * One Slot Survival mod のコマンド処理
 * /oneslot admin validity/role サブコマンドを提供
 */
public class CommandRegisterer {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("oneslot")
                .then(Commands.literal("admin")
                    .requires(source -> source.hasPermission(2)) // OP レベル 2 が必要
                    .then(Commands.literal("validity")
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
                                .executes(context -> toggleOneSlotMode(context, EntityArgument.getPlayers(context, "players"))))))
                    .then(Commands.literal("role")
                        .then(Commands.literal("get")
                            .executes(context -> getRoleStatus(context, getDefaultPlayers(context)))
                            .then(Commands.argument("players", EntityArgument.players())
                                .executes(context -> getRoleStatus(context, EntityArgument.getPlayers(context, "players")))))
                        .then(Commands.literal("set")
                            .then(Commands.argument("roleName", StringArgumentType.word())
                                .executes(context -> setRole(context, getDefaultPlayers(context)))
                                .then(Commands.argument("players", EntityArgument.players())
                                    .executes(context -> setRole(context, EntityArgument.getPlayers(context, "players"))))))
                        .then(Commands.literal("clear")
                            .executes(context -> clearRole(context, getDefaultPlayers(context)))
                            .then(Commands.argument("players", EntityArgument.players())
                                .executes(context -> clearRole(context, EntityArgument.getPlayers(context, "players")))))))
        );
    }

    /**
     * プレイヤーセレクタが省略された場合のデフォルトプレイヤーを取得
     * プレイヤーが発行: @s (自分自身)
     * サーバーコンソール等: @a (全プレイヤー)
     */
    private static Collection<ServerPlayer> getDefaultPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        // プレイヤーが発行した場合は自分自身
        if (source.getEntity() instanceof ServerPlayer player) {
            return java.util.Collections.singleton(player);
        }

        // それ以外（サーバーコンソール等）は全プレイヤー
        return source.getServer().getPlayerList().getPlayers();
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

    private static int setRole(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
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