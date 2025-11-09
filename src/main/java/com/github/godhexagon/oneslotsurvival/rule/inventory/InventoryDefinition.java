package com.github.godhexagon.oneslotsurvival.rule.inventory;

/**
 * Inventory indexについて、そのスロットがMODゲームルール上の定義を参照する。
 * Inventory indexは、AbstractContainerScreenやAbstractContainerMenuのslot/slotIdとは異なることに注意する。
 */
public class InventoryDefinition {
    public static final int ROLE_SLOT_START_INVENTORY_INDEX = 1;

    public static boolean isDisableSlot(int index) {
        return 7 <= index && index <= 35;
    }

    public static boolean isRoleSlot(int index) {
        return 1 <= index && index <= 6;
    }

    public static boolean isRestrictedSlot(int index) {
        return isDisableSlot(index) || isRoleSlot(index);
    }

    public static int getRoleSlotIndex(int index) {
        return index -1;
    }
}
