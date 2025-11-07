package com.github.godhexagon.oneslotsurvival.rule.rolechanging;

import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;

import net.minecraft.server.level.ServerPlayer;

public class RoleChanging {
    public static void change(ServerPlayer player, MainRole role) {
        Exp.clear(player);
        RoleLeveledUpTimes.resetMain(player);
        RoleManager.setRole(player, role);
    }
}
