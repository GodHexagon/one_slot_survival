package com.github.godhexagon.oneslotsurvival.world.util;

/**
 * Inventory indexについて、そのスロットがMODゲームルール上の定義を参照する。
 * Inventory indexは、AbstractContainerScreenやAbstractContainerMenuのslot/slotIdとは異なることに注意する。
 */
public class InventoryDefinition {
    public static boolean isDisableSlot(int index) {
        return 4 <= index && index <= 35;
    }

    public static boolean isRoleSlot(int index) {
        return 1 <= index && index <= 3;
    }

    public static boolean isRestrictedSlot(int index) {
        return isDisableSlot(index) || isRoleSlot(index);
    }

    public static int getRoleSlotIndex(int index) {
        return index -1;
    }
}
