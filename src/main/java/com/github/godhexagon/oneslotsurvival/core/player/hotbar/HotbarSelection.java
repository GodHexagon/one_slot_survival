package com.github.godhexagon.oneslotsurvival.core.player.hotbar;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class HotbarSelection {
    private static final int MAX_SLOT_INDEX = 8;

    /**
     * Force hotbar selection to remain at slot 0 (main hand) during client ticks.
     * Phase 3.2: Disable hotbar scrolling by forcing selection to main hand.
     */
    public static void enforceMainHandSelection() {
        enforceMainHandSelection(4);
    }

    public static void enforceMainHandSelection(int exclusiveEnd) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Use reflection to access private selected field if available
        try {
            var inventory = mc.player.getInventory();
            var selectedField = inventory.getClass().getDeclaredField("selected");
            selectedField.setAccessible(true);
            int currentSelected = selectedField.getInt(inventory);

            int threshold = Math.floorDiv(HotbarSelection.MAX_SLOT_INDEX + exclusiveEnd, 2);
            if (threshold < currentSelected) {
                selectedField.setInt(inventory, exclusiveEnd - HotbarSelection.MAX_SLOT_INDEX -1 + currentSelected);
            } else if (exclusiveEnd <= currentSelected) {
                selectedField.setInt(inventory, currentSelected - exclusiveEnd);
            }
        } catch (Exception e) {
            // Reflection failed, silently ignore
        }
    }
}
