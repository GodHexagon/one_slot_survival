package com.github.godhexagon.oneslotsurvival.core.player.hotbar;

import net.minecraft.client.Minecraft;

public class HotbarSelection {
    /**
     * Force hotbar selection to remain at slot 0 (main hand) during client ticks.
     * Phase 3.2: Disable hotbar scrolling by forcing selection to main hand.
     */
    public static void enforceMainHandSelection() {
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

            if (currentSelected != 0) {
                selectedField.setInt(inventory, 0);
            }
        } catch (Exception e) {
            // Reflection failed, silently ignore
        }
    }
}
