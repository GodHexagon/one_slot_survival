package com.github.godhexagon.oneslotsurvival.world.command.general;

import com.github.godhexagon.oneslotsurvival.object.gamerule.ModGameRules;
import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerProgress;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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
     * ロール名の補完候補を提供するサジェスチョンプロバイダー
     */
    private static final SuggestionProvider<CommandSourceStack> ROLE_SUGGESTIONS = (context, builder) -> {
        // ERRORとUNASSIGNEDを除く全てのメインロールのコマンド名を補完候補として提供
        for (MainRole role : MainRole.values()) {
            if (role != MainRole.ERROR) {
                builder.suggest(role.getCommandName());
            }
        }
        // ERRORとUNASSIGNEDを除く全てのサブロールのコマンド名を補完候補として提供
        for (SubRole role : SubRole.values()) {
            if (role != SubRole.ERROR) {
                builder.suggest(role.getCommandName());
            }
        }
        return builder.buildFuture();
    };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("changerole")
            .then(Commands.argument("roleName", StringArgumentType.word())
                .suggests(ROLE_SUGGESTIONS)
                .then(Commands.literal("info")
                    .executes(ChangeRoleCommand::showChangeInfo))
                .then(Commands.literal("agree")
                    .executes(ChangeRoleCommand::changeRole))
                .executes(ChangeRoleCommand::showChangeInfo));
    }

    private static int showChangeInfo(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            String roleName = StringArgumentType.getString(context, "roleName");

            // メインロールとサブロールの両方から検索
            Role role = null;
            Role currentRole = null;
            MainRole foundMainRole = MainRole.fromCommandName(roleName);
            if (foundMainRole != MainRole.ERROR) {
                boolean mainRoleChangingEnabled = player.level().getGameRules().getBoolean(ModGameRules.MAIN_ROLE_CHANGING);
                if (!mainRoleChangingEnabled) {
                    showRuleRejectionMessages(player, source, roleName, false);
                    return 0;
                }
                role = foundMainRole;
                currentRole = RoleManager.getMainRole(player);
            } else {
                SubRole foundSubRole = SubRole.fromCommandName(roleName);
                if (foundSubRole != SubRole.ERROR) {
                    boolean subRoleChangingEnabled = player.level().getGameRules().getBoolean(ModGameRules.SUB_ROLE_CHANGIN);
                    if (!subRoleChangingEnabled) {
                        showRuleRejectionMessages(player, source, roleName, true);
                        return 0;
                    }
                    role = foundSubRole;
                    currentRole = RoleManager.getSubRole(player);
                }
            }

            if (role == null) {
                context.getSource().sendFailure(
                    Component.literal("Invalid role name: " + roleName + ".")
                );
                return 0;
            }

            double expLost = Exp.getExpMayBeLostToClear(player);
            
            // 現在のロールと同じかチェック
            if (currentRole == role) {
                player.sendSystemMessage(
                    Component.literal("You are already ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(role.getDisplayName().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                        .append(Component.literal(".").withStyle(ChatFormatting.YELLOW))
                );
                return 0;
            }

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
                    .append(concatRolePrefix(role).copy()
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
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
                    .append(Component.literal("")
                        .withStyle(ChatFormatting.WHITE)
                        .append(currentRole.getDisplayName().copy()
                            .withStyle(ChatFormatting.BOLD))
                        .append(" (Level up ")
                        .append(Component.literal("" + leveledUpTimes)
                            .withStyle(ChatFormatting.BOLD))
                        .append(" time(s))"))
            );
            player.sendSystemMessage(
                Component.literal("    • Experience points: ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(String.format("%.1f", expLost))
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
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            String roleName = StringArgumentType.getString(context, "roleName");

            // ゲームルールチェック
            boolean roleChangingEnabled = player.level().getGameRules().getBoolean(ModGameRules.MAIN_ROLE_CHANGING);
            if (!roleChangingEnabled) {
                player.sendSystemMessage(getRuleRejectionMessage());
                // 管理者権限がある場合のみ代替コマンドを提示
                if (source.hasPermission(2)) {
                    player.sendSystemMessage(getAlternativeCommandMessage(roleName, false));// TODO: 仮実装
                }
                return 0;
            }

            // ロール名の検証
            MainRole role = MainRole.fromCommandName(roleName);

            if (role == null || role == MainRole.ERROR) {
                player.sendSystemMessage(
                    Component.literal("Invalid role name: " + roleName + ".")
                );
                return 0;
            }

            // 現在のロールと同じかチェック
            MainRole currentRole = RoleManager.getRole(player);
            if (currentRole == role) {
                player.sendSystemMessage(
                    Component.literal("You are already ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(role.getDisplayName().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD))
                        .append(Component.literal(".").withStyle(ChatFormatting.YELLOW))
                );
                return 0;
            }

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

        // クリック可能なgameruleコマンドリンク
        String commandLiteral = subRole? "/gamerule subRoleChanging/oneslotsurvival true" : "/gamerule roleChanging/oneslotsurvival true";
        Component gameruleCommand = Component.literal(commandLiteral)
            .withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent.SuggestCommand(commandLiteral))
                .withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW)))
            );

        return Component.literal("You can proceed with administrator privileges by using command ")
            .append(adminCommand)
            .append(". Alternatively, you can change the game rules to allow all players to change their roles: ")
            .append(gameruleCommand);
    }
}
