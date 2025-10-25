package com.github.godhexagon.oneslotsurvival.world.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * One Slot Survival mod のコマンド処理
 * /oneslot admin サブコマンドを提供
 */
public class CommandRegisterer {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("oneslot")
                .then(Commands.literal("admin")
                    .requires(source -> source.hasPermission(2)) // OP レベル 2 が必要
                    .then(ValidityCommands.build())
                    .then(RoleCommands.build())
                    .then(LevelCommands.build())
                    .then(ExpCommands.build()))
        );
    }
}
