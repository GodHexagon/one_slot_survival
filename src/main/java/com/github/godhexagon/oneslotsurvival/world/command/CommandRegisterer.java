package com.github.godhexagon.oneslotsurvival.world.command;

import com.github.godhexagon.oneslotsurvival.world.command.admin.LevelCommands;
import com.github.godhexagon.oneslotsurvival.world.command.admin.RoleCommands;
import com.github.godhexagon.oneslotsurvival.world.command.admin.RoleLeveledUpTimesCommands;
import com.github.godhexagon.oneslotsurvival.world.command.admin.ValidityCommands;
import com.github.godhexagon.oneslotsurvival.world.command.admin.XpCommands;
import com.github.godhexagon.oneslotsurvival.world.command.general.ChangeRoleCommand;
import com.github.godhexagon.oneslotsurvival.world.command.general.ShowGameData;
import com.github.godhexagon.oneslotsurvival.world.command.general.ShowNext;
import com.github.godhexagon.oneslotsurvival.world.command.general.ShowStatus;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * One Slot Survival mod のコマンド処理
 * /oneslot サブコマンドを提供
 */
public class CommandRegisterer {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            ShowStatus.buildAlias("oneslot")
                .then(ShowStatus.buildAlias("status"))
                .then(ShowNext.build())
                .then(ChangeRoleCommand.build())
                .then(ShowGameData.build())
                .then(buildAdmin("admin"))
        );
        dispatcher.register(buildAdmin("osa"));
    }
    
    private static LiteralArgumentBuilder<CommandSourceStack> buildAdmin(String aliasName) {
        return Commands.literal(aliasName)
            // 管理者向けコマンド（OP権限必要）
            .requires(source -> source.hasPermission(2)) // OP レベル 2 が必要
            .then(ValidityCommands.build())
            .then(RoleCommands.build())
            .then(LevelCommands.build())
            .then(XpCommands.build())
            .then(RoleLeveledUpTimesCommands.build());
    }
}
