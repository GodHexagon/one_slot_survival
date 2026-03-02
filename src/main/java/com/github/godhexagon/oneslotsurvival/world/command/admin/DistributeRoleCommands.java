package com.github.godhexagon.oneslotsurvival.world.command.admin;

import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.world.util.RoleDistribution;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ロール一括配布コマンド
 * /oneslot admin distributeRole サブコマンド群
 */
public class DistributeRoleCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("distributeRole")
            .then(Commands.literal("unassign")
                .executes(DistributeRoleCommands::unassignAll))
            .then(Commands.literal("random")
                .executes(DistributeRoleCommands::randomDistribute))
            .then(Commands.literal("definedList")
                .executes(DistributeRoleCommands::definedListDistribute));
    }

    /**
     * すべてのオンラインプレイヤーのロールを未割り当てにする
     */
    private static int unassignAll(CommandContext<CommandSourceStack> context) {
        try {
            List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();

            if (players.isEmpty()) {
                context.getSource().sendFailure(Component.literal("No players online to unassign roles."));
                return 0;
            }

            for (ServerPlayer player : players) {
                RoleManager.clearRole(player);
                player.sendSystemMessage(
                    Component.literal("Your roles have been unassigned by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            context.getSource().sendSuccess(
                () -> Component.literal("Unassigned all roles for " + players.size() + " players."),
                true
            );

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * すべてのオンラインプレイヤーにランダムにロールを割り当てる
     */
    private static int randomDistribute(CommandContext<CommandSourceStack> context) {
        try {
            List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();

            if (players.isEmpty()) {
                context.getSource().sendFailure(Component.literal("No players online to distribute roles."));
                return 0;
            }

            for (ServerPlayer player : players) {
                // RoleDistributionを使用してランダムにロールを割り当て
                RoleDistribution.setRandom(player);

                // プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("You have been assigned a random role by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            context.getSource().sendSuccess(
                () -> Component.literal("Randomly distributed roles to " + players.size() + " players."),
                true
            );

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 定義済みリストに基づいてロールを順番に割り当てる
     * プレイヤーはランダムにシャッフルされ、リストは繰り返し使用される
     */
    private static int definedListDistribute(CommandContext<CommandSourceStack> context) {
        try {
            List<ServerPlayer> players = new ArrayList<>(
                context.getSource().getServer().getPlayerList().getPlayers()
            );

            if (players.isEmpty()) {
                context.getSource().sendFailure(Component.literal("No players online to distribute roles."));
                return 0;
            }

            // プレイヤーをランダムにシャッフル
            Collections.shuffle(players);

            // プレイヤーの連番
            int callCount = 0;
            // 各プレイヤーにリストから順番に割り当て
            for (ServerPlayer player : players) {
                // RoleDistributionを使用してロールを割り当て
                RoleDistribution.setDefinedList(player, callCount);

                // プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("You have been assigned a role from the defined list by " +
                        context.getSource().getDisplayName().getString())
                );

                // プレイヤーの連番をインクリメント
                callCount++;
            }

            context.getSource().sendSuccess(
                () -> Component.literal("Distributed roles from defined list to " + players.size() + " players."),
                true
            );

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
