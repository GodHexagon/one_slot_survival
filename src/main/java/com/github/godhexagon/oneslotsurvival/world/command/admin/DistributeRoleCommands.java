package com.github.godhexagon.oneslotsurvival.world.command.admin;

import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerProgress;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * ロール一括配布コマンド
 * /oneslot admin distributeRole サブコマンド群
 */
public class DistributeRoleCommands {

    /**
     * 定義済みのメインロールリスト（繰り返し使用）
     */
    private static final List<MainRole> DEFINED_MAIN_ROLES = List.of(
        MainRole.MINER,
        MainRole.WARRIOR,
        MainRole.SURVIVOR,
        MainRole.BUILDER
    );

    /**
     * 定義済みのサブロールリスト（繰り返し使用）
     */
    private static final List<SubRole> DEFINED_SUB_ROLES = List.of(
        SubRole.ARMORER,
        SubRole.ARCHER,
        SubRole.FISHER,
        SubRole.BREEDER,
        SubRole.SCHOLAR,
        SubRole.PHARMACIST,
        SubRole.THROWER
    );

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

            Random random = new Random();

            // 有効なメインロール（ERROR, UNASSIGNEDを除く）
            List<MainRole> validMainRoles = new ArrayList<>();
            for (MainRole role : MainRole.values()) {
                if (role != MainRole.ERROR && role != MainRole.UNASSIGNED) {
                    validMainRoles.add(role);
                }
            }

            // 有効なサブロール（ERROR, UNASSIGNEDを除く）
            List<SubRole> validSubRoles = new ArrayList<>();
            for (SubRole role : SubRole.values()) {
                if (role != SubRole.ERROR && role != SubRole.UNASSIGNED) {
                    validSubRoles.add(role);
                }
            }

            for (ServerPlayer player : players) {
                // ランダムにメインロールを選択
                MainRole mainRole = validMainRoles.get(random.nextInt(validMainRoles.size()));
                SubRole subRole = validSubRoles.get(random.nextInt(validSubRoles.size()));

                // ロールを設定（プログレスリセット付き）
                PlayerProgress.changeRole(player, mainRole);
                PlayerProgress.changeRole(player, subRole);

                // プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("You have been assigned ")
                        .append(mainRole.getDisplayName())
                        .append(" and ")
                        .append(subRole.getDisplayName())
                        .append(" by " + context.getSource().getDisplayName().getString())
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

            // 各プレイヤーにリストから順番に割り当て
            for (int i = 0; i < players.size(); i++) {
                ServerPlayer player = players.get(i);

                // メインロールをリストから循環的に取得
                MainRole mainRole = DEFINED_MAIN_ROLES.get(i % DEFINED_MAIN_ROLES.size());
                // サブロールをリストから循環的に取得
                SubRole subRole = DEFINED_SUB_ROLES.get(i % DEFINED_SUB_ROLES.size());

                // ロールを設定（プログレスリセット付き）
                PlayerProgress.changeRole(player, mainRole);
                PlayerProgress.changeRole(player, subRole);

                // プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("You have been assigned ")
                        .append(mainRole.getDisplayName())
                        .append(" and ")
                        .append(subRole.getDisplayName())
                        .append(" by " + context.getSource().getDisplayName().getString())
                );
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
