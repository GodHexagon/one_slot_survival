package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * レベルアップ報酬のデータ管理を担当するレジストリ
 * <p>
 * このクラスは、レベルアップ報酬のデータ提供のみを担当します。
 * UI表示やメッセージ送信は、UI層（LevelRewardNotifier等）が担当します。
 * データ層とUI層を分離することで、単一責任の原則に従い、保守性を向上させます。
 * </p>
 */
public class LevelRewardRegistry {
    /**
     * 指定されたロールの全レベル報酬を取得
     * <p>
     * このメソッドは、ロールの全レベルで得られる報酬のリストを返します。
     * コマンド表示などで使用され、報酬システムの変更が自動的に反映されます。
     * </p>
     *
     * @param role 対象のロール
     * @return レベル報酬のリスト（レベル順）
     */
    @Deprecated
    public static List<LevelReward> getAllRewards(MainRole role) {
        List<LevelReward> rewards = new ArrayList<>();

        // ロールスロットを持たない場合は空リスト
        if (!role.hasRoleSlots()) {
            return rewards;
        }

        // 各スロットをレベル報酬として登録
        List<TagKey<Item>> slotTags = role.getSlotItemTags();
        for (int i = 0; i < slotTags.size(); i++) {
            int level = i + 1; // 0-based → 1-based
            TagKey<Item> tag = slotTags.get(i);
            rewards.add(new LevelReward.UnlockSlot(level, i, tag));
        }

        return rewards;
    }

    /**
     * 指定されたレベルアップ回数の報酬を取得
     * <p>
     * 特定のレベルアップ回数で得られる報酬を取得します。
     * </p>
     *
     * @param role 対象のロール
     * @param levelUpTimesInRole ロール内でのレベルアップ回数（1-based）
     * @return レベル報酬（存在しない場合は空）
     */
    @Deprecated
    public static Optional<LevelReward> getRewardForLevelUpTimesInRole(MainRole role, int levelUpTimesInRole) {
        return getAllRewards(role).stream()
            .filter(reward -> reward.levelUpTimesInRole() == levelUpTimesInRole)
            .findFirst();
    }
}
