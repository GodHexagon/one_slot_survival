package com.github.godhexagon.oneslotsurvival.world.command.world;

import com.github.godhexagon.oneslotsurvival.object.storage.WorldOptions;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/**
 * 初期MOD有効性設定を管理するコマンドクラス。
 * <p>/osw defaultModValidity <true|false></p>
 */
public class DefaultModValidityCommand {

    /**
     * defaultModValidity コマンドを構築
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("defaultModValidity")
            .then(Commands.argument("enabled", BoolArgumentType.bool())
                .executes(context -> setDefaultModValidity(context, BoolArgumentType.getBool(context, "enabled"))))
            .executes(DefaultModValidityCommand::queryDefaultModValidity);
    }

    private static int setDefaultModValidity(CommandContext<CommandSourceStack> context, boolean enabled) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            // WorldOptionsを使用して設定
            WorldOptions.setDefaultModValidityEnabled(server, enabled);

            // 成功メッセージ
            source.sendSuccess(
                () -> Component.literal("Default mod validity has been set to ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(String.valueOf(enabled))
                        .withStyle(enabled ? ChatFormatting.AQUA : ChatFormatting.RED, ChatFormatting.BOLD)),
                true
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int queryDefaultModValidity(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            // WorldOptionsを使用して取得
            boolean currentValue = WorldOptions.isDefaultModValidityEnabled(server);

            source.sendSuccess(
                () -> Component.literal("Default mod validity is currently ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(String.valueOf(currentValue))
                        .withStyle(currentValue ? ChatFormatting.GREEN : ChatFormatting.RED, ChatFormatting.BOLD)),
                false
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
