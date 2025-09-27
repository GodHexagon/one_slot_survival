package com.github.godhexagon.oneslotsurvival;

import net.minecraft.world.entity.player.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Simple manager for tracking which players have One Slot mode enabled.
 * Uses a HashSet for fast lookups without requiring complex capability system.
 */
public class OneSlotManager {
    private static final Set<UUID> enabledPlayers = new HashSet<>();

    /**
     * Check if One Slot mode is enabled for the given player.
     */
    public static boolean isEnabled(Player player) {
        return enabledPlayers.contains(player.getUUID());
    }

    /**
     * Enable or disable One Slot mode for the given player.
     */
    public static void setEnabled(Player player, boolean enabled) {
        if (enabled) {
            enabledPlayers.add(player.getUUID());
        } else {
            enabledPlayers.remove(player.getUUID());
        }
    }

    /**
     * Toggle One Slot mode for the given player.
     * @return the new state (true if now enabled, false if now disabled)
     */
    public static boolean toggle(Player player) {
        boolean newState = !isEnabled(player);
        setEnabled(player, newState);
        return newState;
    }

    /**
     * Get the number of players with One Slot mode enabled.
     */
    public static int getEnabledPlayerCount() {
        return enabledPlayers.size();
    }

    /**
     * Clear all enabled players (for testing or reset purposes).
     */
    public static void clearAll() {
        enabledPlayers.clear();
    }
}