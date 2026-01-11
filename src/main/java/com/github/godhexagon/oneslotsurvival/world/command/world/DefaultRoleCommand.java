package com.github.godhexagon.oneslotsurvival.world.command.world;

import com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils;
import com.github.godhexagon.oneslotsurvival.world.storage.DefaultRole;
import com.github.godhexagon.oneslotsurvival.world.storage.WorldOptions;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/**
 * 初期ロール割り当て方式設定を管理するコマンドクラス。
 * <p>/osw defaultRole <unassign|random|definedList></p>
 * <p>/osw defaultRole (照会)</p>
 */
public class DefaultRoleCommand {

    /**
     * defaultRole コマンドを構築
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("defaultRole")
            .then(Commands.argument("roleType", StringArgumentType.word())
                .suggests(CommandUtils.DEFAULT_ROLE_SUGGESTIONS)
                .executes(DefaultRoleCommand::setDefaultRole))
            .executes(DefaultRoleCommand::queryDefaultRole);
    }

    /**
     * 初期ロール割り当て方式設定を変更する
     */
    private static int setDefaultRole(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            String roleTypeName = StringArgumentType.getString(context, "roleType");
            DefaultRole defaultRole = DefaultRole.fromCommandName(roleTypeName);

            // WorldOptionsを使用して設定
            WorldOptions.setDefaultRole(server, defaultRole);

            // 成功メッセージ
            source.sendSuccess(
                () -> Component.literal("Default role assignment has been set to ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(defaultRole.getDisplayName())
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)),
                true
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 初期ロール割り当て方式設定を照会する
     */
    private static int queryDefaultRole(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            // WorldOptionsを使用して取得
            DefaultRole currentRole = WorldOptions.getDefaultRole(server);

            source.sendSuccess(
                () -> Component.literal("Default role assignment is currently ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(currentRole.getDisplayName())
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)),
                false
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
