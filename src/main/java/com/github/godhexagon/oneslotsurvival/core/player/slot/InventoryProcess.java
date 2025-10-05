package com.github.godhexagon.oneslotsurvival.core.player.slot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryProcess {
    public static final int MAIN_HAND = 0;
    public static final int ROLE_START = 1;
    public static final int ROLE_END = 3;
    public static final int PROHIBITED_START = 4;
    public static final int PROHIBITED_END = Inventory.INVENTORY_SIZE - 1;

    /**
     * Public method to process all inventory restrictions for a player.
     * Used both by tick events and when initially enabling the mode.
     */
    public static void processInventoryRestrictions(Player player) {
        // Clear items from prohibited slots (1-35)
        clearProhibitedSlots(player);
    }

    /**
     * Clear items from prohibited slots (hotbar slots 1-8 and inventory slots 9-35)
     * Items are moved to main hand if possible, otherwise dropped on ground
     * Then place barrier items in prohibited slots
     */
    private static void clearProhibitedSlots(Player player) {
        Inventory inventory = player.getInventory();

        // Check slots 1-35 (prohibited slots)
        for (int slot = ROLE_START; slot <= PROHIBITED_END; slot++) {
            ItemStack item = inventory.getItem(slot);
            boolean isRoleSlot = slot <= ROLE_END;

            // If slot contains a barrier item, leave it alone
            if (isRoleSlot) {
                if (RoleSlotBarrier.isBarrierItem(item)) {
                    continue;
                }
            } else {
                if (SlotBarrier.isBarrierItem(item)) {
                    continue;
                }
            }

            if (!item.isEmpty()) {
                player.drop(item, false);
            }

            if (isRoleSlot) {
                inventory.setItem(slot, RoleSlotBarrier.createBarrierStack());
            } else {
                inventory.setItem(slot, SlotBarrier.createBarrierStack());
            }
        }
    }

    /**
     * Clear all barrier items from prohibited slots (1-35) when restrictions are disabled
     */
    public static void clearBarrierItems(Player player) {
        Inventory inventory = player.getInventory();

        // Clear barrier items from slots 1-35 (prohibited slots)
        for (int slot = PROHIBITED_START; slot <= PROHIBITED_END; slot++) {
            ItemStack item = inventory.getItem(slot);

            // If slot contains a barrier item, remove it
            if (SlotBarrier.isBarrierItem(item)) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    public static boolean isProcessedInventoryRestrictions(Player player) {
        // Check inventory
        for (int i = PROHIBITED_START; i <= PROHIBITED_END; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (SlotBarrier.isBarrierItem(stack)) {
                return true;
            }
        }

        return false;
    }
}
