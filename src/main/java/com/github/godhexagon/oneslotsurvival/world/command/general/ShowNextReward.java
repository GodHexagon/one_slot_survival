package com.github.godhexagon.oneslotsurvival.world.command.general;

import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.Level;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.LevelReward;
import com.github.godhexagon.oneslotsurvival.rule.role.LevelRewardRegistry;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.world.cui.TextFormatUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 次のレベルアップ報酬表示コマンド（一般プレイヤー向け）
 * /oneslot next - 次のレベルアップ報酬を表示
 */
public class ShowNextReward {

    public static LiteralArgumentBuilder<CommandSourceStack> buildAlias(String aliasName) {
        return Commands.literal(aliasName)
            .executes(ShowNextReward::showNextReward);
    }

    private static int showNextReward(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            MainRole role = RoleManager.getRole(player);
            int level = Level.get(player);
            double remaining = Exp.get(player);
            int leveledUpTimes = RoleLeveledUpTimes.getMain(player);

            // デザインされたメッセージを送信
            player.sendSystemMessage(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.GRAY)
            );
            player.sendSystemMessage(
                Component.literal("  ").append(
                    Component.literal("⚔ Next Reward")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                )
            );
            player.sendSystemMessage(Component.literal(""));

            // 次のレベルアップ報酬を取得（現在のロールレベルアップ回数より上で最初に見つかる報酬）
            var allRewards = LevelRewardRegistry.getAllRewards(role);
            var nextReward = allRewards.stream()
                .filter(reward -> reward.levelUpTimesInRole() > leveledUpTimes)
                .findFirst();

            if (nextReward.isPresent() && nextReward.get() instanceof LevelReward.UnlockSlot unlockSlot) {
                int rewardLevelUpTimes = unlockSlot.levelUpTimesInRole();
                String slotOrdinal = TextFormatUtils.getOrdinal(unlockSlot.slotNumber());

                // スロットの説明を翻訳キーから取得
                String descriptionKey = "oneslotsurvival.slot." + role.getCommandName() + "." + unlockSlot.slotIndex() + ".description";

                // 報酬を得るまでに必要なレベルアップ回数を計算
                int levelUpsNeeded = rewardLevelUpTimes - leveledUpTimes;

                // 報酬を得られるゲーム内レベルを計算
                int rewardGameLevel = level + levelUpsNeeded;

                // 報酬を得るまでに必要な経験値を計算
                // 現在のレベルから必要なレベルアップ回数分の累積経験値
                // 次のレベルまでの必要経験値量はremainingでわかる。remaining分があれば1レベル上がるのでデクリメント。
                double totalExpNeeded = remaining;
                levelUpsNeeded--;
                for (int i = 0; i < levelUpsNeeded; i++) {
                    totalExpNeeded += Exp.getNextLevelExp(level + i);
                }
                // 現在の残り経験値を加算
                double expToReward = totalExpNeeded;

                player.sendSystemMessage(
                    Component.literal("  Level " + rewardGameLevel + " ")
                        .withStyle(ChatFormatting.AQUA)
                        .append(Component.literal("(Remaining " + String.format("%.0f", expToReward) + " exp)")
                            .withStyle(ChatFormatting.GRAY))
                        .append(Component.literal(": ")
                            .withStyle(ChatFormatting.AQUA))
                        .append(Component.literal("Unlock " + slotOrdinal + " slot ")
                            .withStyle(ChatFormatting.GREEN))
                        .append(Component.translatable(descriptionKey)
                            .withStyle(ChatFormatting.GRAY))
                );
            } else {
                player.sendSystemMessage(
                    Component.literal("  No more rewards available")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                );
            }

            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.GRAY)
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
