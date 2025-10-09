package com.github.godhexagon.oneslotsurvival;

import net.minecraft.world.entity.player.Player;

/**
 * Manager for tracking which players have One Slot mode enabled.
 * Uses simple properties file persistence for world-wide data.
 */
public class OneSlotManager {

    /**
     * Check if One Slot mode is enabled for the given player.
     */
    public static boolean isEnabled(Player player) {
        return false; // TODO
    }

    /**
     * Enable or disable One Slot mode for the given player.
     * When enabling, processes existing inventory items.
     */
    public static void setEnabled(Player player, boolean enabled) {
        // TODO
    }

    /**
     * Toggle One Slot mode for the given player.
     * @return the new state (true if now enabled, false if now disabled)
     */
    public static boolean toggle(Player player) {
        return false; // TODO
    }
}