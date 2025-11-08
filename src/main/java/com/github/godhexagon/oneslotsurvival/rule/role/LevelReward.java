package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * レベルアップ報酬を表すデータクラス
 * <p>
 * レベルアップ時に付与される報酬の種類と詳細情報を保持します。
 * このクラスはイミュータブルであり、報酬のデータ表現のみを担当します。
 * </p>
 */
public sealed interface LevelReward permits LevelReward.UnlockSlot {

    /**
     * この報酬が付与される条件となるレベルアップ回数
     * <p>
     * 現在のロール内で経験したレベルアップの回数を表します。
     * 例: levelUpTimesInRole = 1 の場合、そのロールで1回レベルアップすると報酬を獲得
     * </p>
     *
     * @return レベルアップ回数（1-based）
     */
    int levelUpTimesInRole();

    /**
     * 報酬の種類
     *
     * @return 報酬タイプ
     */
    RewardType type();

    /**
     * 報酬の種類を表す列挙型
     */
    enum RewardType {
        /**
         * ロールスロットの解放
         */
        UNLOCK_SLOT
        // 将来の拡張: UNLOCK_ABILITY, INCREASE_STAT, etc.
    }

    /**
     * スロット解放報酬
     *
     * @param levelUpTimesInRole ロール内でのレベルアップ回数（1-based）
     * @param slotIndex スロットインデックス（0-based）
     * @param itemTag このスロットで使用可能なアイテムタグ
     */
    record UnlockSlot(int levelUpTimesInRole, int slotIndex, TagKey<Item> itemTag) implements LevelReward {

        @Override
        public RewardType type() {
            return RewardType.UNLOCK_SLOT;
        }

        /**
         * スロット番号を取得（1-based）
         *
         * @return スロット番号
         */
        public int slotNumber() {
            return slotIndex + 1;
        }
    }
}
