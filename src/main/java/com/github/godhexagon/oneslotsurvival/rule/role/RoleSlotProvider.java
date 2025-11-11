package com.github.godhexagon.oneslotsurvival.rule.role;

import java.util.List;
import java.util.Optional;

interface RoleSlotProvider {
    /**
     * このロールで使用可能になるロールスロットを取得する
     * <p>
     * インデックスはロールスロットのインデックスに対応します。
     * 例: index 0 = 1番目のロールスロット（つるはし等）
     * </p>
     * 
     * @return ロールスロット
     */
    public List<RoleSlot> getRoleSlots();

    /**
     * 特定のレベルにおいて利用可能なロールスロットをすべて取得する
     * <p>
     * インデックスはロールスロットのインデックスに対応します。
     * 例: index 0 = 1番目のロールスロット（つるはし等）
     * </p>
     * 
     * @return ロールスロット
     */
    public List<RoleSlot> getAvailableRoleSlots(int levelUpTimesInRoleToAvailable);
    
    /**
     * 指定されたロールスロットで許可されるアイテムタグを取得
     * <p>
     * プレイヤーのロールとスロットインデックスから、そのスロットで使用可能な
     * アイテムタグを返します。
     * </p>
     *
     * @param index ロールスロットのインデックス（0から始まる）
     * @param levelUpTimesInRole そのロールでレベルアップを経験した回数
     * @return 許可されるアイテムタグ。ロールがスロットを持たない、またはロールが未開放の場合は空
     */
    public Optional<RoleSlot> getAvailableRoleSlot(int index, int levelUpTimesInRole);
    
    /**
     * ロールスロットを持つかどうか
     *
     * @return ロールスロットを持つ場合 true
     */
    public boolean hasRoleSlots();
}
