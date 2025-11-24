package com.github.godhexagon.oneslotsurvival.world.command.general;

import com.github.godhexagon.oneslotsurvival.cui.CuiUtil;
import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.Level;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;
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
 * レベル・経験値確認コマンド（一般プレイヤー向け）
 * /oneslot level - 自分のレベルと残り経験値を表示
 * /oneslot exp - level のエイリアス
 */
public class ShowStatus {

    public static LiteralArgumentBuilder<CommandSourceStack> buildAlias(String aliasName) {
        return Commands.literal(aliasName)
            .executes(ShowStatus::showStatus);
    }

    private static int showStatus(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            MainRole mainRole = RoleManager.getMainRole(player);
            SubRole subRole = RoleManager.getSubRole(player);
            int level = Level.get(player);
            double remaining = Exp.get(player);

            // デザインされたメッセージを送信
            player.sendSystemMessage(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.GRAY)
            );
            player.sendSystemMessage(
                Component.literal("  ").append(
                    Component.literal("⚔ One Slot Survival Status")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                )
            );
            player.sendSystemMessage(Component.literal(""));

            int leveledUpTimes = RoleLeveledUpTimes.getMain(player);
            player.sendSystemMessage(
                Component.literal("  Main Role: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(createRoleNameMessage(mainRole, leveledUpTimes))
            );
            player.sendSystemMessage(
                Component.literal("  Sub Role: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(createRoleNameMessage(subRole, leveledUpTimes))
            );
            player.sendSystemMessage(
                Component.literal("  Level: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.valueOf(level))
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
            );
            player.sendSystemMessage(
                Component.literal("  Exp to reach the next level: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.format("%.1f", remaining))
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            );
            player.sendSystemMessage(Component.literal(""));
            
        // クリック可能なコマンドリンクを作成
        String commandLiteral = "/oneslot next";
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
                .append(Component.literal(" to view your next level up rewards.")
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

    private static MutableComponent createRoleNameMessage(Role role, int leveledUpTimes) {
        return CuiUtil.addRoleCommandSugguestion(
            Component.literal("")
                .append(role.getDisplayName().copy()
                    .withStyle(ChatFormatting.BOLD))
                .append(" (Level up ")
                .append(Component.literal("" + leveledUpTimes)
                    .withStyle(ChatFormatting.BOLD))
                .append(" time(s))")
                .withStyle(style -> style
                    .withColor(ChatFormatting.LIGHT_PURPLE))
        , role);
    }
}
