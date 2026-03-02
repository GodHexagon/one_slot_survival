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
 * ロール変更設定を管理するコマンドクラス。
 * <p>/osw mainRoleChanging <true|false></p>
 * <p>/osw subRoleChanging <true|false></p>
 */
public class RoleChanging {

    /**
     * mainRoleChanging コマンドを構築
     */
    public static LiteralArgumentBuilder<CommandSourceStack> buildMainRoleChanging() {
        return Commands.literal("mainRoleChanging")
            .then(Commands.argument("enabled", BoolArgumentType.bool())
                .executes(context -> setMainRoleChanging(context, BoolArgumentType.getBool(context, "enabled"))))
            .executes(RoleChanging::queryMainRoleChanging);
    }

    /**
     * subRoleChanging コマンドを構築
     */
    public static LiteralArgumentBuilder<CommandSourceStack> buildSubRoleChanging() {
        return Commands.literal("subRoleChanging")
            .then(Commands.argument("enabled", BoolArgumentType.bool())
                .executes(context -> setSubRoleChanging(context, BoolArgumentType.getBool(context, "enabled"))))
            .executes(RoleChanging::querySubRoleChanging);
    }

    private static int setMainRoleChanging(CommandContext<CommandSourceStack> context, boolean enabled) {
        return setSetting(context, enabled, "Main role changing", true);
    }

    private static int setSubRoleChanging(CommandContext<CommandSourceStack> context, boolean enabled) {
        return setSetting(context, enabled, "Sub role changing", false);
    }

    private static int queryMainRoleChanging(CommandContext<CommandSourceStack> context) {
        return querySetting(context, "Main role changing", true);
    }

    private static int querySubRoleChanging(CommandContext<CommandSourceStack> context) {
        return querySetting(context, "Sub role changing", false);
    }

    /**
     * 設定値を変更する共通メソッド
     */
    private static int setSetting(CommandContext<CommandSourceStack> context, boolean value, String settingName, boolean isMainRole) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            // WorldOptionsを使用して設定
            if (isMainRole) {
                WorldOptions.setMainRoleChangingEnabled(server, value);
            } else {
                WorldOptions.setSubRoleChangingEnabled(server, value);
            }

            // 成功メッセージ
            source.sendSuccess(
                () -> Component.literal(settingName + " has been set to ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(String.valueOf(value))
                        .withStyle(value ? ChatFormatting.AQUA : ChatFormatting.RED, ChatFormatting.BOLD)),
                true
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 設定値を照会する共通メソッド
     */
    private static int querySetting(CommandContext<CommandSourceStack> context, String settingName, boolean isMainRole) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            // WorldOptionsを使用して取得
            boolean currentValue = isMainRole ?
                WorldOptions.isMainRoleChangingEnabled(server) :
                WorldOptions.isSubRoleChangingEnabled(server);

            source.sendSuccess(
                () -> Component.literal(settingName + " is currently ")
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
