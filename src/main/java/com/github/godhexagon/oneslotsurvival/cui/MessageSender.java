package com.github.godhexagon.oneslotsurvival.cui;

import java.util.ArrayList;
import java.util.List;
import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.Level;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleSlot;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerPlayer;

public class MessageSender {
    /**
     * レベルアップ報酬を表示
     *
     * @param player プレイヤー
     */
    public static void displayLevelUpRewards(ServerPlayer player) {
        // レベルアップ祝福
        Component congratsMessage = Component.literal("Congratulations! ")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(Component.literal("You've reached Level " + Level.get(player) + "!")
                        .withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(congratsMessage);
    
        // 報酬スロットの情報を収集
        List<Component> slotDescriptions = new ArrayList<>();

        // メインロールの報酬スロットを取得
        MainRole mainRole = RoleManager.getMainRole(player);
        int mainLevelUpTimes = RoleLeveledUpTimes.getMain(player);
        List<RoleSlot> mainRewards = mainRole.getJustUnlockedRoleSlots(mainLevelUpTimes);
        for (RoleSlot reward: mainRewards) {
            slotDescriptions.add(CuiObjects.createSlotDescription(reward, mainRole));
        }

        // サブロールの報酬スロットを取得
        SubRole subRole = RoleManager.getSubRole(player);
        int subLevelUpTimes = RoleLeveledUpTimes.getSub(player);
        List<RoleSlot> subRoleSlots = subRole.getJustUnlockedRoleSlots(subLevelUpTimes);
        for (RoleSlot reward: subRoleSlots) {
            slotDescriptions.add(CuiObjects.createSlotDescription(reward, subRole));
        }

        // 報酬がある場合のみヘッダーを表示
        if (!slotDescriptions.isEmpty()) {
            player.sendSystemMessage(Component.literal("  Level Up Rewards:")
                    .withStyle(ChatFormatting.AQUA));
        }

        for (Component m: slotDescriptions) {
            player.sendSystemMessage(
                Component.literal("    ● ")
                    .append(m)
            );
        }

        // クリック可能なコマンドリンクを作成
        String commandLiteral = "/oneslot next";
        Component clickableCommand = Component.literal(commandLiteral)
            .withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent.SuggestCommand(commandLiteral))
                .withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW)))
            );

        player.sendSystemMessage(
            Component.literal("  Use ")
                .withStyle(ChatFormatting.GRAY)
                .append(clickableCommand)
                .append(Component.literal(" to view your next level up rewards.")
                    .withStyle(ChatFormatting.GRAY))
        );
    }

    public static void displayNextReward(ServerPlayer player) {
        // 次回以降初めて報酬がもらえるレベルで説明を追加する
        List<Component> slotDescriptions = new ArrayList<>();
        int levelUpTimesToReward = 0;
        int mainLevelUpTimes = RoleLeveledUpTimes.getMain(player);
        MainRole mainRole = RoleManager.getMainRole(player);
        int subLevelUpTimes = RoleLeveledUpTimes.getSub(player);
        SubRole subRole = RoleManager.getSubRole(player);

        while (slotDescriptions.isEmpty()) {
            levelUpTimesToReward++;
            int mnl = mainLevelUpTimes + levelUpTimesToReward;
            int snl = subLevelUpTimes + levelUpTimesToReward;
            
            // メインロールの報酬スロットを取得
            List<RoleSlot> mainRewards = mainRole.getJustUnlockedRoleSlots(mnl);
            for (RoleSlot reward: mainRewards) {
                slotDescriptions.add(CuiObjects.createSlotDescription(reward, mainRole));
            }

            // サブロールの報酬スロットを取得
            List<RoleSlot> subRoleSlots = subRole.getJustUnlockedRoleSlots(snl);
            for (RoleSlot reward: subRoleSlots) {
                slotDescriptions.add(CuiObjects.createSlotDescription(reward, subRole));
            }
            
            if (mainRole.getAvailableRoleSlots(mnl).size() >= mainRole.getRoleSlots().size() &&
                subRole.getAvailableRoleSlots(snl).size() >= subRole.getRoleSlots().size()) 
            {
                break;
            }
        }

        // 報酬がもらえるレベル
        int levelToReward = levelUpTimesToReward + Level.get(player);
        // 報酬を得るまでに必要な経験値を計算
        // 現在のレベルから必要なレベルアップ回数分の累積経験値
        // 次のレベルまでの必要経験値量はExp.getでわかる。
        double totalExpNeeded = Exp.get(player);
        for (int level = 1 + Level.get(player); level < levelToReward; level++) {
            totalExpNeeded += Exp.getNextLevelExp(level);
        }
        double expToReward = totalExpNeeded;

        // デザインされたメッセージを送信
        player.sendSystemMessage(
            Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(ChatFormatting.GRAY)
        );
        player.sendSystemMessage(
            Component.literal("  ").append(
                Component.literal("⚔ Your Next Level Up Rewards")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
            )
        );
        player.sendSystemMessage(Component.literal(""));

        if (slotDescriptions.isEmpty()) {
            player.sendSystemMessage(
                Component.literal("  You have received all the rewards!")
                    .withStyle(ChatFormatting.YELLOW)
            );
        } else {
            player.sendSystemMessage(
                Component.literal("  ").append(
                    Component.literal("You will receive reward at level ")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal(String.valueOf(levelToReward))
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                        .append(Component.literal(" (Required "))
                        .append(Component.literal(String.format("%.1f", expToReward))
                            .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
                        .append(Component.literal(" EXP)"))
                )
            );
            
            player.sendSystemMessage(Component.literal(""));

            for (Component m: slotDescriptions) {
                player.sendSystemMessage(
                    Component.literal("    ● ")
                        .append(m)
                );
            }
        }

        player.sendSystemMessage(Component.literal(""));

        // クリック可能なコマンドリンクを作成
        String commandLiteral = "/oneslot status";
        Component clickableCommand = Component.literal(commandLiteral)
            .withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent.SuggestCommand(commandLiteral))
                .withHoverEvent(new HoverEvent.ShowText(
                    Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW)))
            );

        player.sendSystemMessage(
            Component.literal("  Use ")
                .withStyle(ChatFormatting.GRAY)
                .append(clickableCommand)
                .append(Component.literal(" to view your status.")
                    .withStyle(ChatFormatting.GRAY))
        );

        player.sendSystemMessage(
            Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(ChatFormatting.GRAY)
        );
    }
}
