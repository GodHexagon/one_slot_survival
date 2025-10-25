package com.github.godhexagon.oneslotsurvival.world.command.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

/**
 * コマンド処理の共通ユーティリティ
 */
public class CommandUtils {

    /**
     * プレイヤーセレクタが省略された場合のデフォルトプレイヤーを取得
     * プレイヤーが発行: @s (自分自身)
     * サーバーコンソール等: @a (全プレイヤー)
     */
    public static Collection<ServerPlayer> getDefaultPlayers(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();

        // プレイヤーが発行した場合は自分自身
        if (source.getEntity() instanceof ServerPlayer player) {
            return java.util.Collections.singleton(player);
        }

        // それ以外（サーバーコンソール等）は全プレイヤー
        return source.getServer().getPlayerList().getPlayers();
    }
}
