package com.github.godhexagon.oneslotsurvival.server.player;

import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Manager for tracking which players have One Slot mode enabled.
 * Uses the Attribute system for automatic persistence and client synchronization.
 */
public class PlayerModValidity {

    /**
     * Check if One Slot mode is enabled for the given player.
     * Works on both client and server side - automatically synchronized.
     *
     * @param player the player to check
     * @return true if One Slot mode is enabled for this player
     */
    public static boolean isEnabled(Player player) {
        AttributeInstance attribute = player.getAttribute(ModAttributes.MOD_ENABLED.getHolder().get());
        if (attribute == null) {
            return false;
        }
        // Value > 0.5 means enabled (using 0.5 as threshold for floating point safety)
        return attribute.getBaseValue() > 0.5;
    }

    /**
     * Enable or disable One Slot mode for the given player.
     * When enabling, processes existing inventory items.
     * Automatically persisted and synchronized to client.
     *
     * @param player the player to modify
     * @param enabled true to enable One Slot mode, false to disable
     */
    public static void setEnabled(Player player, boolean enabled) {
        AttributeInstance attribute = player.getAttribute(ModAttributes.MOD_ENABLED.getHolder().get());
        if (attribute == null) {
            throw new IllegalStateException("Player does not have MOD_ENABLED attribute");
        }

        // Set base value: 1.0 for enabled, 0.0 for disabled
        attribute.setBaseValue(enabled ? 1.0 : 0.0);

        // TODO: Process existing inventory items when enabling
    }

    /**
     * Toggle One Slot mode for the given player.
     * Automatically persisted and synchronized to client.
     *
     * @param player the player to toggle
     * @return the new state (true if now enabled, false if now disabled)
     */
    public static boolean toggle(Player player) {
        boolean newState = !isEnabled(player);
        setEnabled(player, newState);
        return newState;
    }
}