package com.github.godhexagon.oneslotsurvival.core.player.slot;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class InventoryProcess {
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
        for (int slot = 1; slot < 36; slot++) {
            ItemStack item = inventory.getItem(slot);

            // If slot contains a barrier item, leave it alone
            if (BarrierItem.isBarrierItem(item)) {
                continue;
            }

            if (item.isEmpty()) {
                // Slot is empty, place barrier
                inventory.setItem(slot, BarrierItem.createBarrierStack());
                continue;
            }

            // Try to move item to main hand (slot 0)
            if (moveItemToMainHand(player, item, slot)) {
                // Successfully moved, place barrier
                inventory.setItem(slot, BarrierItem.createBarrierStack());
            } else {
                // Drop item on ground using vanilla API
                player.drop(item, false);
                // Place barrier after dropping
                inventory.setItem(slot, BarrierItem.createBarrierStack());
            }
        }
    }

    /**
     * Try to move an item to the main hand slot (slot 0)
     * @return true if item was successfully moved, false if main hand is full
     */
    private static boolean moveItemToMainHand(Player player, ItemStack item, int sourceSlot) {
        Inventory inventory = player.getInventory();
        ItemStack mainHandItem = inventory.getItem(0);

        if (mainHandItem.isEmpty()) {
            // Main hand is empty, move item there
            inventory.setItem(0, item.copy());
            return true;
        } else if (ItemStack.isSameItemSameComponents(mainHandItem, item)) {
            // Same item type, try to stack
            int combinedCount = mainHandItem.getCount() + item.getCount();
            int maxStackSize = mainHandItem.getMaxStackSize();

            if (combinedCount <= maxStackSize) {
                // Can stack completely
                mainHandItem.setCount(combinedCount);
                return true;
            } else {
                // Partial stack - fill main hand to max and leave remainder
                int remainder = combinedCount - maxStackSize;
                mainHandItem.setCount(maxStackSize);
                item.setCount(remainder);
                return false; // Still have remainder to drop
            }
        }

        return false; // Cannot move to main hand
    }

    /**
     * Clear all barrier items from prohibited slots (1-35) when restrictions are disabled
     */
    public static void clearBarrierItems(Player player) {
        Inventory inventory = player.getInventory();

        // Clear barrier items from slots 1-35 (prohibited slots)
        for (int slot = 1; slot < 36; slot++) {
            ItemStack item = inventory.getItem(slot);

            // If slot contains a barrier item, remove it
            if (BarrierItem.isBarrierItem(item)) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }
}
