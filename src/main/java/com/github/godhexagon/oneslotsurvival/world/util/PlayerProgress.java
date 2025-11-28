package com.github.godhexagon.oneslotsurvival.world.util;

import com.github.godhexagon.oneslotsurvival.cui.MessageSender;
import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;

import net.minecraft.server.level.ServerPlayer;

public class PlayerProgress {
    public static void recieveExp(ServerPlayer player, double amount) {
        // 経験値獲得が許可されていないプレイヤーをはじく
        if (!RoleManager.isPossibleRoleProgress(player)) {
            return;
        }

        if (Exp.add(player, amount)) {
            MessageSender.displayLevelUpRewards(player);
        }
    }
    

    /**
     * プレイヤーのロールを変更（経験値とレベルアップ回数をリセット）
     *
     * @param player 変更するプレイヤー
     * @param role 設定するロール
     */
    public static void changeRole(ServerPlayer player, Role role) {
        Exp.clear(player);
        if (role instanceof MainRole mainRole) {
            RoleLeveledUpTimes.resetMain(player);
            RoleManager.setMainRole(player, mainRole);
        } else if (role instanceof SubRole subRole) {
            RoleLeveledUpTimes.resetSub(player);
            RoleManager.setSubRole(player, subRole);
        }
    }
}
