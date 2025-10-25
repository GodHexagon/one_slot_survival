package com.github.godhexagon.oneslotsurvival.world.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * One Slot Survival mod のコマンド処理
 * /oneslot サブコマンドを提供
 */
public class CommandRegisterer {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("oneslot")
                // 一般プレイヤー向けコマンド（権限不要）
                .then(LevelCommand.build())  // level コマンド
                .then(LevelCommand.buildAlias("exp"))  // exp は level のエイリアス
                // 管理者向けコマンド（OP権限必要）
                .then(Commands.literal("admin")
                    .requires(source -> source.hasPermission(2)) // OP レベル 2 が必要
                    .then(ValidityCommands.build())
                    .then(RoleCommands.build())
                    .then(LevelCommands.build())
                    .then(ExpCommands.build()))
        );
    }
}
