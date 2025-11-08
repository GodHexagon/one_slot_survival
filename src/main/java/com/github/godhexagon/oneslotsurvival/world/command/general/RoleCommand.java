package com.github.godhexagon.oneslotsurvival.world.command.general;

import com.github.godhexagon.oneslotsurvival.rule.role.LevelReward;
import com.github.godhexagon.oneslotsurvival.rule.role.LevelRewardRegistry;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils;
import com.github.godhexagon.oneslotsurvival.world.cui.LevelRewardFormatter;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

/**
 * ロール詳細表示コマンド（一般プレイヤー向け）
 * /oneslot role detail [&lt;Role&gt;]
 * /oneslot role list
 */
public class RoleCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("role")
            .executes(RoleCommand::showRoleList)
            .then(Commands.literal("list")
                .executes(RoleCommand::showRoleList))
            .then(Commands.literal("detail")
                .executes(RoleCommand::showRoleDetailWithPlayerRole)
                .then(Commands.argument("roleName", StringArgumentType.word())
                    .suggests(CommandUtils.ROLE_SUGGESTIONS)
                    .executes(RoleCommand::showRoleDetailWithSpecifiedRole)));
    }

    /**
     * すべてのロール一覧を表示
     */
    private static int showRoleList(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            // ヘッダー
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(
                Component.literal("Available Roles")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
            );
            player.sendSystemMessage(Component.literal(""));

            // すべてのロールを表示（ERROR と UNASSIGNED を除く）
            for (MainRole role : MainRole.values()) {
                if (role == MainRole.ERROR || role == MainRole.UNASSIGNED) {
                    continue;
                }

                // ロール名
                Component roleDisplayName = role.getDisplayName();

                // /oneslot role detail <role> へのクリック可能なリンク
                String detailCommand = "/oneslot role detail " + role.getCommandName();
                Component clickableRoleName = roleDisplayName.copy()
                    .withStyle(style -> style
                        .withColor(ChatFormatting.LIGHT_PURPLE)
                        .withBold(true)
                        .withClickEvent(new ClickEvent.SuggestCommand(detailCommand))
                        .withHoverEvent(new HoverEvent.ShowText(
                            Component.literal("Click to view details").withStyle(ChatFormatting.YELLOW)))
                    );

                // ロールの表示
                player.sendSystemMessage(
                    Component.literal("  • ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(clickableRoleName)
                );
            }

            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(
                Component.literal("Click on a role name to see details")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
            player.sendSystemMessage(Component.literal(""));

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * プレイヤー自身のロールの詳細を表示
     */
    private static int showRoleDetailWithPlayerRole(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            MainRole playerRole = RoleManager.getRole(player);

            // ロールが未割り当てかチェック
            if (playerRole == MainRole.UNASSIGNED || playerRole == MainRole.ERROR) {
                player.sendSystemMessage(
                    Component.literal("You don't have a role yet. Please get a role first.")
                        .withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            return showRoleDetail(player, playerRole);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 指定されたロールの詳細を表示
     */
    private static int showRoleDetailWithSpecifiedRole(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            String roleName = StringArgumentType.getString(context, "roleName");

            // ロール名の検証
            MainRole role = MainRole.fromCommandName(roleName);
            if (role == null || role == MainRole.ERROR || role == MainRole.UNASSIGNED) {
                player.sendSystemMessage(
                    Component.literal("Invalid role name: " + roleName + ".")
                        .withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            return showRoleDetail(player, role);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * ロール詳細を表示する共通処理
     */
    private static int showRoleDetail(ServerPlayer player, MainRole role) {
        // ロール表示名
        Component roleDisplayName = role.getDisplayName();

        // ヘッダー
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(
            Component.literal("Role ")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                .append(roleDisplayName.copy().withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
        );
        player.sendSystemMessage(Component.literal(""));

        // レベルアップ報酬の説明（データ駆動方式）
        List<LevelReward> rewards = LevelRewardRegistry.getAllRewards(role);

        if (rewards.isEmpty()) {
            player.sendSystemMessage(
                Component.literal("This role has no level up rewards.")
                    .withStyle(ChatFormatting.GRAY)
            );
        } else {
            player.sendSystemMessage(
                Component.literal("Level Up Rewards:")
                    .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
            );
            player.sendSystemMessage(Component.literal(""));

            for (int i = 0; i < rewards.size(); i++) {
                LevelReward reward = rewards.get(i);

                // 報酬タイプに応じた表示を生成
                if (reward instanceof LevelReward.UnlockSlot unlockSlot) {
                    LevelRewardFormatter.displayUnlockSlotRewardDetailed(player, role, unlockSlot);
                }
                // 将来の拡張: 他の報酬タイプもここで処理

                // 最後のアイテム以外は空行を追加
                if (i < rewards.size() - 1) {
                    player.sendSystemMessage(Component.literal(""));
                }
            }
        }

        player.sendSystemMessage(Component.literal(""));
        
        String commandLiteral = "/oneslot changerole " + role.getCommandName();
        Component clickableCommand = Component.literal(commandLiteral)
            .withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent.SuggestCommand(commandLiteral))
                .withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW)))
            );
        player.sendSystemMessage(Component.literal("To change to this role, use ")
            .withStyle(ChatFormatting.GRAY)
            .append(clickableCommand)
            .append("."));

        return 1;
    }

}
