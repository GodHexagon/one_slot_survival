package com.github.godhexagon.oneslotsurvival.rule.inventory;

/**
 * Inventoryのコンテナインデックス(Inventory Index)について、そのインデックスに対するMODゲームルール上の定義を提供する。
 * Inventory Indexは、AbstractContainerScreenやAbstractContainerMenuのslot/slotIdとは異なることに注意する。
 */
public class InventoryDefinition {
    public static final int ROLE_SLOT_START_INVENTORY_INDEX = 1;

    /**
     * 指定されたインデックスが無効化対象スロットかどうかを返します。
     * これがtrueのときはユーザーによるそのスロットの操作が常に禁止されるべきです。
     *
     * @param index Inventory Index。
     * @return 無効化対象スロットであるとき、true。
     */
    public static boolean isDisableSlot(int index) {
        return 7 <= index && index <= 35;
    }

    /**
     * 指定されたインデックスがロールスロットかどうかを返します。
     * これがtrueのときは、ユーザーによるそのスロットの操作について、レベルとロールに合わせたコントロールが適用される可能性があります。
     *
     * @param index Inventory Index。
     * @return ロールスロットであるとき、true。
     */
    public static boolean isRoleSlot(int index) {
        return 1 <= index && index <= 6;
    }

    /**
     * 指定されたインデックスが制限対象であるかどうかを返します。制限対象とは、ロールスロットと無効化対象スロットの二つを指します。
     * 
     * @param index Inventory Index
     * @return そのスロットが制限対象であるときtrue
     */
    public static boolean isRestrictedSlot(int index) {
        return isDisableSlot(index) || isRoleSlot(index);
    }

    /**
     * Inventory IndexからRole Slot Indexへ変換します。
     *
     * @param index Inventory Index。
     * @return Role Slot Index。
     */
    public static int getRoleSlotIndex(int index) {
        return index -1;
    }
}
