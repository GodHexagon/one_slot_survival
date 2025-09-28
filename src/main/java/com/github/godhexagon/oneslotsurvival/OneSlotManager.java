package com.github.godhexagon.oneslotsurvival;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.GameType;

import java.util.EnumSet;
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
        Set<GameType> effectiveGamemode = EnumSet.of(GameType.SURVIVAL, GameType.ADVENTURE);
        return enabledPlayers.contains(player.getUUID()) && effectiveGamemode.contains(player.gameMode());
    }

    /**
     * Enable or disable One Slot mode for the given player.
     * When enabling, processes existing inventory items.
     */
    public static void setEnabled(Player player, boolean enabled) {
        UUID playerId = player.getUUID();
        boolean wasEnabled = enabledPlayers.contains(playerId);

        if (enabled) {
            enabledPlayers.add(playerId);

            // Process existing inventory when first enabling
            if (!wasEnabled) {
                player.displayClientMessage(
                    Component.literal("§6[One Slot] Mode enabled. Only main hand slot can be used."),
                    false
                );
            }
        } else {
            enabledPlayers.remove(playerId);

            if (wasEnabled) {
                player.displayClientMessage(
                    Component.literal("§6[One Slot] Mode disabled. All inventory slots available."),
                    false
                );
            }
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