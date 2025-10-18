package com.github.godhexagon.oneslotsurvival.world.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class SlotDefinition {
    public static final int INVENTORY_SIZE = Inventory.INVENTORY_SIZE;
    public static final int HOTBAR_SIZE = Inventory.SELECTION_SIZE;

    /**
     * 制限対象のスロットかどうかを判定
     *
     * @param slot スロットID（AbstractContainerMenuのスロットID）
     * @return true の場合、制限対象
     */
    public static boolean isRestrictedSlot(int slot) {
        // インベントリスロット（9-35）を制限
        if (slot >= 9 && slot <= 35) {
            return true;
        }

        // ホットバーの範囲を制限
        if (slot >= 40 && slot <= 44) {
            return true;
        }

        // その他のスロット（クラフト、防具、オフハンド、メインハンド）は制限しない
        return false;
    }

    /**
     * ロールスロットかどうかを判定
     * ロールスロット = メインスロット（インデックス0）の右側に3つあるスロット
     *
     * @param index スロットID（AbstractContainerMenuのスロットID）
     * @return true の場合、ロールスロット
     */
    public static boolean isRoleSlot(int index) {
        // ホットバーの範囲を制限
        return 37 <= index && index <= 39;
    }

    /**
     * つるはしかどうかを判定
     *
     * @param stack 判定対象のアイテムスタック
     * @return true の場合、つるはし
     */
    public static boolean isPickaxe(ItemStack stack) {
        // TODO: つるはしの判定ロジックを実装する
        // 候補:
        // - item instanceof PickaxeItem
        // - ItemTags.PICKAXES
        // - 特定のアイテムID
        return false; // 仮実装
    }
}
