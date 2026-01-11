package com.github.godhexagon.oneslotsurvival.world.command.general;

import com.github.godhexagon.oneslotsurvival.cui.CuiObjects;
import com.github.godhexagon.oneslotsurvival.cui.CuiUtil;
import com.github.godhexagon.oneslotsurvival.world.storage.WorldOptions;
import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;
import com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerProgress;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

/**
 * ロール変更コマンド（一般プレイヤー向け）
 *
 * <p>このコマンドはゲームルール「roleChanging」がtrueの時のみ使用可能</p>
 */
public class ChangeRoleCommand {
    /**
     * ロール変更の入力検証結果を保持する内部クラス
     */
    private static class RoleChangeValidationResult {
        final boolean success;
        final ServerPlayer player;
        final Role targetRole;
        final Role currentRole;

        private RoleChangeValidationResult(boolean success, ServerPlayer player, Role targetRole, Role currentRole) {
            this.success = success;
            this.player = player;
            this.targetRole = targetRole;
            this.currentRole = currentRole;
        }

        static RoleChangeValidationResult success(ServerPlayer player, Role targetRole, Role currentRole) {
            return new RoleChangeValidationResult(true, player, targetRole, currentRole);
        }

        static RoleChangeValidationResult failure() {
            return new RoleChangeValidationResult(false, null, null, null);
        }
    }

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("changerole")
            .then(Commands.argument("roleName", StringArgumentType.word())
                .suggests(CommandUtils.ROLE_SUGGESTIONS)
                .then(Commands.literal("info")
                    .executes(ChangeRoleCommand::showChangeInfo))
                .then(Commands.literal("agree")
                    .executes(ChangeRoleCommand::changeRole))
                .executes(ChangeRoleCommand::showChangeInfo))
            .executes(ChangeRoleCommand::showList);
    }

    private static int showList(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }
            
            // 表示できるロールがあるかチェック
            boolean mainRoleChangingEnabled = WorldOptions.isMainRoleChangingEnabled(player.level());
            boolean subRoleChangingEnabled = WorldOptions.isSubRoleChangingEnabled(player.level());
            if (!mainRoleChangingEnabled && !subRoleChangingEnabled) {
                //
                player.sendSystemMessage(getRuleRejectionMessage());
                // 管理者権限がある場合のみ代替コマンドを提示
                if (source.hasPermission(2)) {
                    player.sendSystemMessage(getAlternativeCommandMessage(RoleManager.getMainRole(player).getCommandName(), false));
                    player.sendSystemMessage(getAlternativeCommandMessage(RoleManager.getSubRole(player).getCommandName(), true));
                }
                return 0;
            }

            // ヘッダー
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(
                Component.literal("Select to change")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
            );
            player.sendSystemMessage(Component.literal(""));

            // 二つのリスト
            if (mainRoleChangingEnabled) {
                player.sendSystemMessage(
                    Component.literal("Main Roles")
                        .withStyle(ChatFormatting.YELLOW)
                );
                for (Component line: CuiObjects.createRoleList(false, true)) {
                    player.sendSystemMessage(Component.literal("  ").append(line));
                }
                player.sendSystemMessage(Component.literal(""));
            }
            
            if (subRoleChangingEnabled) {
                player.sendSystemMessage(
                    Component.literal("Sub Roles")
                        .withStyle(ChatFormatting.YELLOW)
                );
                for (Component line: CuiObjects.createRoleList(true, true)) {
                    player.sendSystemMessage(Component.literal("  ").append(line));
                }
                player.sendSystemMessage(Component.literal(""));
            }

            // フッター
            player.sendSystemMessage(
                Component.literal("Click on a role name to change it")
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
     * ロール変更の入力検証を行う共通メソッド
     * <p>検証失敗時は適切なエラーメッセージを送信し、{@code RoleChangeValidationResult.failure()}を返す。
     * 検証成功時は必要な情報を含む{@code RoleChangeValidationResult}を返す。</p>
     *
     * @param context コマンドコンテキスト
     * @return 検証結果
     */
    private static RoleChangeValidationResult validateRoleChange(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // プレイヤーが発行した場合のみ動作
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return RoleChangeValidationResult.failure();
        }

        String roleName = StringArgumentType.getString(context, "roleName");

        // メインロールとサブロールの両方から検索
        Role role = null;
        Role currentRole = null;
        MainRole foundMainRole = MainRole.fromCommandName(roleName);
        if (foundMainRole != MainRole.ERROR) {
            boolean mainRoleChangingEnabled = WorldOptions.isMainRoleChangingEnabled(player.level());
            if (!mainRoleChangingEnabled) {
                showRuleRejectionMessages(player, source, roleName, false);
                return RoleChangeValidationResult.failure();
            }
            role = foundMainRole;
            currentRole = RoleManager.getMainRole(player);
        } else {
            SubRole foundSubRole = SubRole.fromCommandName(roleName);
            if (foundSubRole != SubRole.ERROR) {
                boolean subRoleChangingEnabled = WorldOptions.isSubRoleChangingEnabled(player.level());
                if (!subRoleChangingEnabled) {
                    showRuleRejectionMessages(player, source, roleName, true);
                    return RoleChangeValidationResult.failure();
                }
                role = foundSubRole;
                currentRole = RoleManager.getSubRole(player);
            }
        }

        if (role == null) {
            source.sendFailure(
                Component.literal("Invalid role name: " + roleName + ".")
            );
            return RoleChangeValidationResult.failure();
        }

        // 現在のロールと同じかチェック
        if (currentRole == role) {
            player.sendSystemMessage(
                Component.literal("You are already ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(role.getDisplayName().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                    .append(Component.literal(".").withStyle(ChatFormatting.YELLOW))
            );
            return RoleChangeValidationResult.failure();
        }

        return RoleChangeValidationResult.success(player, role, currentRole);
    }

    private static int showChangeInfo(CommandContext<CommandSourceStack> context) {
        try {
            // 入力検証
            RoleChangeValidationResult validation = validateRoleChange(context);
            if (!validation.success) {
                return 0;
            }

            ServerPlayer player = validation.player;
            Role role = validation.targetRole;
            Role currentRole = validation.currentRole;
            String roleName = StringArgumentType.getString(context, "roleName");

            // 情報表示
            player.sendSystemMessage(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.GRAY)
            );
            player.sendSystemMessage(
                Component.literal("  ").append(
                    Component.literal("Preview ⚠ You haven't changed your role yet.")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                )
            );
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(
                Component.literal("  Changing to: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(CuiUtil.addRoleCommandSugguestion(
                        concatRolePrefix(role).copy()
                            .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)
                        , role))
            );
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(
                Component.literal("  Progress that will be lost:")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
            );
            
            int leveledUpTimes = RoleLeveledUpTimes.getMain(player);
            player.sendSystemMessage(
                Component.literal("    • Progress of role ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(CuiUtil.addRoleCommandSugguestion(
                        Component.literal("")
                            .withStyle(ChatFormatting.WHITE)
                            .append(currentRole.getDisplayName().copy()
                                .withStyle(ChatFormatting.BOLD))
                            .append(" (Level up ")
                            .append(Component.literal("" + leveledUpTimes)
                                .withStyle(ChatFormatting.BOLD))
                            .append(" time(s))")
                        , currentRole))
            );
            player.sendSystemMessage(
                Component.literal("    • Experience points: ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(String.format("%.1f", Exp.getExpMayBeLostToClear(player)))
                        .withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
            );
            player.sendSystemMessage(Component.literal(""));

            // クリック可能なコマンドリンクを作成
            String commandLiteral = "/oneslot changerole " + roleName + " agree";
            Component clickableCommand = Component.literal(commandLiteral)
                .withStyle(style -> style
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent.SuggestCommand(commandLiteral))
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW)))
                );

            player.sendSystemMessage(
                Component.literal("  Use ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(clickableCommand)
                    .append(Component.literal(" to confirm")
                        .withStyle(ChatFormatting.GRAY))
            );
            player.sendSystemMessage(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.GRAY)
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int changeRole(CommandContext<CommandSourceStack> context) {
        try {
            // 入力検証
            RoleChangeValidationResult validation = validateRoleChange(context);
            if (!validation.success) {
                return 0;
            }

            ServerPlayer player = validation.player;
            Role role = validation.targetRole;

            // ロール変更実行
            PlayerProgress.changeRole(player, role);

            String roleCommandName = role.getCommandName();
            Component roleDisplayName = role.getDisplayName();

            player.sendSystemMessage(
                Component.literal("Your role has been changed to " + roleCommandName + " (")
                    .append(roleDisplayName)
                    .append(")")
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static Component concatRolePrefix(Role role) {
        return Component.literal(role instanceof MainRole? "Main Role " : "Sub Role ")
            .append(role.getDisplayName());
    }
    
    private static void showRuleRejectionMessages(ServerPlayer player, CommandSourceStack source, String roleName, boolean subRole) {
        player.sendSystemMessage(getRuleRejectionMessage());
        // 管理者権限がある場合のみ代替コマンドを提示
        if (source.hasPermission(2)) {
            player.sendSystemMessage(getAlternativeCommandMessage(roleName, subRole));
        }
    }

    private static MutableComponent getRuleRejectionMessage() {
        return Component.literal("Changing roles is not permitted according to the rules in this world.")
            .withStyle(ChatFormatting.RED);
    }

    private static MutableComponent getAlternativeCommandMessage(String roleName, boolean subRole) {
        // クリック可能な管理者コマンドリンク
        Component adminCommand = Component.literal("/oneslot admin role set " + roleName)
            .withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent.SuggestCommand("/oneslot admin role set " + roleName))
                .withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW)))
            );

        // クリック可能なdata storageコマンドリンク
        String commandLiteral = subRole?
            "/data modify storage oneslotsurvival:settings subRoleChanging set value 1b" :
            "/data modify storage oneslotsurvival:settings mainRoleChanging set value 1b";
        Component storageCommand = Component.literal(commandLiteral)
            .withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent.SuggestCommand(commandLiteral))
                .withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW)))
            );

        return Component.literal("You can proceed with administrator privileges by using command ")
            .append(adminCommand)
            .append(". Alternatively, you can change the settings to allow all players to change their roles: ")
            .append(storageCommand);
    }
}
