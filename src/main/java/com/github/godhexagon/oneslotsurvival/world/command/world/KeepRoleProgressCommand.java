package com.github.godhexagon.oneslotsurvival.world.command.world;

import com.github.godhexagon.oneslotsurvival.world.storage.WorldOptions;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/**
 * ロール変更時に進捗（レベル・経験値）を保持するかの設定を管理するコマンドクラス。
 * <p>/osw keepRoleProgress <true|false></p>
 * <p>/osw keepRoleProgress (照会)</p>
 */
public class KeepRoleProgressCommand {

    /**
     * keepRoleProgress コマンドを構築
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("keepRoleProgress")
            .then(Commands.argument("enabled", BoolArgumentType.bool())
                .executes(KeepRoleProgressCommand::setKeepRoleProgress))
            .executes(KeepRoleProgressCommand::queryKeepRoleProgress);
    }

    /**
     * ロール変更時に進捗を保持するかの設定を変更する
     */
    private static int setKeepRoleProgress(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            boolean enabled = BoolArgumentType.getBool(context, "enabled");

            // WorldOptionsを使用して設定
            WorldOptions.setKeepRoleProgressEnabled(server, enabled);

            // 成功メッセージ
            source.sendSuccess(
                () -> Component.literal("Keep role progress has been set to ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(enabled ? "enabled" : "disabled")
                        .withStyle(enabled ? ChatFormatting.AQUA : ChatFormatting.RED, ChatFormatting.BOLD)),
                true
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * ロール変更時に進捗を保持するかの設定を照会する
     */
    private static int queryKeepRoleProgress(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            // WorldOptionsを使用して取得
            boolean enabled = WorldOptions.isKeepRoleProgressEnabled(server);

            source.sendSuccess(
                () -> Component.literal("Keep role progress is currently ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(enabled ? "enabled" : "disabled")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD)),
                false
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
