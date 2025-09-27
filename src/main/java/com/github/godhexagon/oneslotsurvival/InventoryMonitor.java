package com.github.godhexagon.oneslotsurvival;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Monitors player inventories for changes and tracks invalid item placements.
 */
public class InventoryMonitor {

    // Store previous inventory state for each player to detect changes
    private static final Map<UUID, List<ItemStack>> previousInventoryStates = new HashMap<>();

    /**
     * Check if the player's inventory has changed and notify about invalid items.
     */
    public static void checkInventoryChange(Player player) {
        if (!OneSlotManager.isEnabled(player)) {
            return;
        }

        UUID playerId = player.getUUID();
        var inventory = player.getInventory();

        // Get current inventory state (slots 0-35: hotbar + main inventory)
        List<ItemStack> currentState = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            currentState.add(inventory.getItem(i).copy());
        }

        // Check if inventory has changed
        List<ItemStack> previousState = previousInventoryStates.get(playerId);
        if (previousState == null || !inventoriesEqual(previousState, currentState)) {
            // Inventory has changed, check for invalid items
            checkInvalidItems(player);

            // Update stored state
            previousInventoryStates.put(playerId, currentState);
        }
    }

    /**
     * Check for items in invalid locations and notify the player.
     */
    private static void checkInvalidItems(Player player) {
        var inventory = player.getInventory();
        List<String> invalidItems = new ArrayList<>();

        // Check slots 1-35 (everything except main hand slot 0)
        for (int i = 1; i < 36; i++) {
            ItemStack item = inventory.getItem(i);
            if (!item.isEmpty()) {
                String slotType = getSlotTypeName(i);
                String itemName = item.getHoverName().getString();
                int count = item.getCount();

                if (count > 1) {
                    invalidItems.add(String.format("§c%s x%d in %s (slot %d)", itemName, count, slotType, i));
                } else {
                    invalidItems.add(String.format("§c%s in %s (slot %d)", itemName, slotType, i));
                }
            }
        }

        // Send notification if there are invalid items
        if (!invalidItems.isEmpty()) {
            player.displayClientMessage(Component.literal("§6[One Slot] Items in invalid locations:"), false);
            for (String invalidItem : invalidItems) {
                player.displayClientMessage(Component.literal("  " + invalidItem), false);
            }
        }
    }

    /**
     * Get human-readable name for slot type.
     */
    private static String getSlotTypeName(int slotIndex) {
        if (slotIndex >= 1 && slotIndex <= 8) {
            return "Hotbar";
        } else if (slotIndex >= 9 && slotIndex <= 35) {
            return "Inventory";
        }
        return "Unknown";
    }

    /**
     * Compare two inventory states for equality.
     */
    private static boolean inventoriesEqual(List<ItemStack> state1, List<ItemStack> state2) {
        if (state1.size() != state2.size()) {
            return false;
        }

        for (int i = 0; i < state1.size(); i++) {
            ItemStack item1 = state1.get(i);
            ItemStack item2 = state2.get(i);

            if (!ItemStack.isSameItemSameComponents(item1, item2) || item1.getCount() != item2.getCount()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Clear stored state for a player (e.g., when they disconnect).
     */
    public static void clearPlayerState(UUID playerId) {
        previousInventoryStates.remove(playerId);
    }

    /**
     * Clear all stored states.
     */
    public static void clearAllStates() {
        previousInventoryStates.clear();
    }
}