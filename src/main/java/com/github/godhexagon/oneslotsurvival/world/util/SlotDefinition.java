package com.github.godhexagon.oneslotsurvival.world.util;

import net.minecraft.world.entity.player.Inventory;

public class SlotDefinition {
    public static final int INVENTORY_SIZE = Inventory.INVENTORY_SIZE;
    public static final int HOTBAR_SIZE = Inventory.SELECTION_SIZE;

    /**
     * 制限対象のスロットかどうかを判定
     *
     * @param index スロットインデックス
     * @return true の場合、制限対象
     */
    private boolean isRestrictedSlot(int index) {
        // インベントリスロット（9-35）を制限
        if (index >= 9 && index <= 35) {
            return true;
        }

        // ホットバースロット（37-44、メインハンド36以外）を制限
        if (index >= 40 && index <= 44) {
            return true;
        }

        // その他のスロット（クラフト、防具、オフハンド、メインハンド）は制限しない
        return false;
    }
}
