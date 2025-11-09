package com.github.godhexagon.oneslotsurvival.world.util;

import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.Level;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.LevelReward;
import com.github.godhexagon.oneslotsurvival.rule.role.LevelRewardRegistry;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.world.cui.LevelRewardFormatter;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class PlayerProgress {
    public static void recieveExp(ServerPlayer player, double amount) {
        // ロールスロットが許可されていないプレイヤーをはじく
        if (!RoleManager.hasRole(player)) {
            return;
        }

        if (Exp.add(player, amount)) {
            // レベルアップ祝福
            Component congratsMessage = Component.literal("Congratulations! ")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                    .append(Component.literal("You've reached Level " + Level.get(player) + "!")
                            .withStyle(ChatFormatting.YELLOW));
            player.sendSystemMessage(congratsMessage);

            // 報酬詳細表示
            int levelUpTimes = RoleLeveledUpTimes.getMain(player);
            MainRole role = RoleManager.getRole(player);
            Optional<LevelReward> rewardOpt = LevelRewardRegistry.getRewardForLevelUpTimesInRole(role, levelUpTimes);

            rewardOpt.ifPresent(reward -> {
                if (reward instanceof LevelReward.UnlockSlot unlockSlot) {
                    notifySlotUnlock(player, unlockSlot);
                }
            });
        }
    }

    /**
     * スロット解放通知を送信
     */
    private static void notifySlotUnlock(ServerPlayer player, LevelReward.UnlockSlot unlockSlot) {
        MainRole role = RoleManager.getRole(player);
        LevelRewardFormatter.displayUnlockSlotRewardNotification(player, role, unlockSlot);
    }
    
    public static void changeRole(ServerPlayer player, MainRole role) {
        Exp.clear(player);
        RoleLeveledUpTimes.resetMain(player);
        RoleManager.setRole(player, role);
    }
}
