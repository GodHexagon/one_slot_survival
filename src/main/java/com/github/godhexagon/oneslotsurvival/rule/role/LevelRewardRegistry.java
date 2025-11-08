package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;

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
     * 指定されたロールスロットで許可されるアイテムタグを取得
     * <p>
     * プレイヤーのロールとスロットインデックスから、そのスロットで使用可能な
     * アイテムタグを返します。
     * </p>
     *
     * @param player プレイヤー
     * @param roleSlotIndex ロールスロットのインデックス（0から始まる）
     * @return 許可されるアイテムタグ。ロールがスロットを持たない、またはロールが未開放の場合は空
     */
    public static Optional<TagKey<Item>> getAllowedItemTag(Player player, int roleSlotIndex) {
        MainRole role = RoleManager.getRole(player);
        int levelUpTimes = RoleLeveledUpTimes.getMain(player);
        return getAllowedItemTag(role, levelUpTimes, roleSlotIndex);
    }

    /**
     * 指定されたロールとスロットインデックスで許可されるアイテムタグを取得
     * <p>
     * ロールのアイテムタグリストから、指定されたインデックスのタグを返します。
     * </p>
     *
     * @param role ロール
     * @param levelUpTimesInRole このロールで経験したレベルアップ回数
     * @param roleSlotIndex ロールスロットのインデックス（0から始まる）
     * @return 許可されるアイテムタグ。ロールがスロットを持たない、またはロールが未開放の場合は空
     */
    public static Optional<TagKey<Item>> getAllowedItemTag(MainRole role, int levelUpTimesInRole, int roleSlotIndex) {
        // ロールスロットを持たないロールか
        if (!role.hasRoleSlots()) {
            return Optional.empty();
        }

        // ルールにあるロールスロットか
        var tags = role.getSlotItemTags();
        if (roleSlotIndex < 0 || roleSlotIndex >= tags.size()) {
            return Optional.empty();
        }
        
        // 解放済みのロールか
        if (levelUpTimesInRole > roleSlotIndex) {
            return Optional.of(tags.get(roleSlotIndex));
        } else {
            return Optional.empty();
        }
    }

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
    public static Optional<LevelReward> getRewardForLevelUpTimesInRole(MainRole role, int levelUpTimesInRole) {
        return getAllRewards(role).stream()
            .filter(reward -> reward.levelUpTimesInRole() == levelUpTimesInRole)
            .findFirst();
    }
}
