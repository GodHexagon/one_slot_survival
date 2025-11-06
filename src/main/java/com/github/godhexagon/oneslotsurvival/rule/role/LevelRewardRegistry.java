package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Optional;

/**
 * レベルアップ報酬の管理を担当するレジストリ
 * <p>
 * このクラスは、レベルアップ時のメッセージ送信やスロット解放の通知など、
 * レベルアップに関連する報酬処理を一元管理します。
 * </p>
 */
public class LevelRewardRegistry {

    /**
     * レベルアップ時の報酬を付与
     * <p>
     * プレイヤーがレベルアップした際に、祝福メッセージと
     * 解放されたスロットの情報を送信します。
     * </p>
     *
     * @param player レベルアップしたプレイヤー
     * @param newLevel 到達した新しいレベル
     */
    public static void grantLevelUpReward(ServerPlayer player, int newLevel) {
        MainRole role = RoleManager.getRole(player);

        // ロールがない場合は何もしない
        if (!RoleManager.hasRole(player)) {
            return;
        }

        // レベルアップメッセージを送信
        sendLevelUpMessage(player, newLevel);

        // スロット解放通知（該当する場合）
        notifySlotUnlock(player, role, newLevel);
    }

    /**
     * レベルアップメッセージを送信
     *
     * @param player プレイヤー
     * @param newLevel 新しいレベル
     */
    private static void sendLevelUpMessage(ServerPlayer player, int newLevel) {
        Component congratsMessage = Component.literal("Congratulations! ")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal("You've reached Level " + newLevel + "!")
                        .withStyle(ChatFormatting.YELLOW));
        player.sendSystemMessage(congratsMessage);
    }

    /**
     * スロット解放通知を送信
     * <p>
     * レベルアップによって新しいロールスロットが解放された場合、
     * そのスロットで使用可能なアイテムタグを通知します。
     * </p>
     *
     * @param player プレイヤー
     * @param role プレイヤーのロール
     * @param newLevel 新しいレベル
     */
    private static void notifySlotUnlock(ServerPlayer player, MainRole role, int newLevel) {
        // レベル → ロールスロットインデックスへの変換
        // レベル2で最初のスロット解放、レベル3で2番目...
        int roleSlotIndex = newLevel - 2;

        // スロットが解放されていない場合は何もしない
        if (roleSlotIndex < 0) {
            return;
        }

        // このレベルで解放されるアイテムタグを取得
        Optional<TagKey<Item>> tagOpt = SlotRegistry.getAllowedItemTag(role, roleSlotIndex);
        if (tagOpt.isEmpty()) {
            return;
        }

        TagKey<Item> capableItemTag = tagOpt.get();
        String tagName = capableItemTag.location().toString();

        Component rewardMessage = Component.literal("Reward: ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal("New role slot unlocked! ")
                        .withStyle(ChatFormatting.WHITE))
                .append(Component.literal("[" + tagName + "]")
                        .withStyle(ChatFormatting.AQUA));
        player.sendSystemMessage(rewardMessage);
    }
}
