package com.github.godhexagon.oneslotsurvival.world.command.general;

import com.github.godhexagon.oneslotsurvival.world.util.level.Exp;
import com.github.godhexagon.oneslotsurvival.world.util.level.Level;
import com.github.godhexagon.oneslotsurvival.world.util.role.MainRole;
import com.github.godhexagon.oneslotsurvival.world.util.role.RoleManager;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
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

            MainRole role = RoleManager.getRole(player);
            int level = Level.getMain(player);
            double remaining = Exp.getMain(player);

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
            player.sendSystemMessage(
                Component.literal("  Role: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(role.getDisplayName()
                        .copy()
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
            );
            player.sendSystemMessage(
                Component.literal("  Level: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.valueOf(level))
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
            );
            player.sendSystemMessage(
                Component.literal("  Remaining EXP until the next level: ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.format("%.1f", remaining))
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
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
}
