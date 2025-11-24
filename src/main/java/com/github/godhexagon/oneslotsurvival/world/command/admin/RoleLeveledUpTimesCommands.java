package com.github.godhexagon.oneslotsurvival.world.command.admin;

import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
 * ロールレベルアップ回数管理コマンド
 * /oneslot admin roleLeveledUpTimes サブコマンド群
 */
public class RoleLeveledUpTimesCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("roleLeveledUpTimes")
            .then(Commands.literal("get")
                .executes(context -> getRoleLeveledUpTimes(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> getRoleLeveledUpTimes(context, EntityArgument.getPlayers(context, "players")))))
            .then(Commands.literal("setMain")
                .then(Commands.argument("times", IntegerArgumentType.integer(0))
                    .executes(context -> setMainRoleLeveledUpTimes(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> setMainRoleLeveledUpTimes(context, EntityArgument.getPlayers(context, "players"))))))
            .then(Commands.literal("setSub")
                .then(Commands.argument("times", IntegerArgumentType.integer(0))
                    .executes(context -> setSubRoleLeveledUpTimes(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> setSubRoleLeveledUpTimes(context, EntityArgument.getPlayers(context, "players"))))))
            .then(Commands.literal("clear")
                .executes(context -> clearRoleLeveledUpTimes(context, getDefaultPlayers(context)))
                .then(Commands.literal("main")
                    .executes(context -> clearMainRoleLeveledUpTimes(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> clearMainRoleLeveledUpTimes(context, EntityArgument.getPlayers(context, "players")))))
                .then(Commands.literal("sub")
                    .executes(context -> clearSubRoleLeveledUpTimes(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> clearSubRoleLeveledUpTimes(context, EntityArgument.getPlayers(context, "players")))))
                .then(Commands.literal("both")
                    .executes(context -> clearRoleLeveledUpTimes(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> clearRoleLeveledUpTimes(context, EntityArgument.getPlayers(context, "players"))))));
    }

    private static int getRoleLeveledUpTimes(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                int mainTimes = RoleLeveledUpTimes.getMain(player);
                int subTimes = RoleLeveledUpTimes.getSub(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Main role leveled up times for " + player.getName().getString() + ": " + mainTimes),
                    false
                );

                context.getSource().sendSuccess(
                    () -> Component.literal("Sub role leveled up times for " + player.getName().getString() + ": " + subTimes),
                    false
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setMainRoleLeveledUpTimes(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            int times = IntegerArgumentType.getInteger(context, "times");

            for (ServerPlayer player : players) {
                RoleLeveledUpTimes.setMain(player, times);

                context.getSource().sendSuccess(
                    () -> Component.literal("Set main role leveled up times to " + times + " for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your main role leveled up times has been set to " + times +
                        " by " + context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setSubRoleLeveledUpTimes(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            int times = IntegerArgumentType.getInteger(context, "times");

            for (ServerPlayer player : players) {
                RoleLeveledUpTimes.setSub(player, times);

                context.getSource().sendSuccess(
                    () -> Component.literal("Set sub role leveled up times to " + times + " for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your sub role leveled up times has been set to " + times +
                        " by " + context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearRoleLeveledUpTimes(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                RoleLeveledUpTimes.resetMain(player);
                RoleLeveledUpTimes.resetSub(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared both role leveled up times for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your both role leveled up times has been reset by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearMainRoleLeveledUpTimes(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                RoleLeveledUpTimes.resetMain(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared main role leveled up times for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your main role leveled up times has been reset by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearSubRoleLeveledUpTimes(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                RoleLeveledUpTimes.resetSub(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared sub role leveled up times for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your sub role leveled up times has been reset by " +
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
