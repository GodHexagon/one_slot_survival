package com.github.godhexagon.oneslotsurvival.core.player.modvalidity;

import com.github.godhexagon.oneslotsurvival.core.player.slot.InventoryProcess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Manager for tracking which players have One Slot mode enabled.
 * Uses simple properties file persistence for world-wide data.
 */
public class Configuration {
    private static final String FILE_NAME = "oneslot_enabled_players.properties";

    private static final Set<UUID> enabledPlayers = new HashSet<>();
    private static boolean dataLoaded = false;

    /**
     * Check if One Slot mode is enabled for the given player.
     */
    public static boolean getValidity(Player player) {
        ensureDataLoaded();
        return enabledPlayers.contains(player.getUUID());
    }

    /**
     * Enable or disable One Slot mode for the given player.
     * When enabling, processes existing inventory items.
     */
    public static void setValidity(Player player, boolean enabled) {
        ensureDataLoaded();

        UUID playerId = player.getUUID();
        boolean wasEnabled = enabledPlayers.contains(playerId);
        boolean isEffectiveGameMode = WorldState.isEffectiveGameMode(player.gameMode());

        if (enabled) {
            enabledPlayers.add(playerId);

            // Process existing inventory when first enabling
            if (!wasEnabled) {
                if (isEffectiveGameMode) {
                    InventoryProcess.processInventoryRestrictions(player);
                }
                player.displayClientMessage(
                    Component.literal("§6[One Slot] Mode enabled. Only main hand slot can be used."),
                    false
                );
            }
        } else {
            enabledPlayers.remove(playerId);

            if (wasEnabled) {
                if (isEffectiveGameMode) {
                    InventoryProcess.clearBarrierItems(player);
                }
                player.displayClientMessage(
                    Component.literal("§6[One Slot] Mode disabled. All inventory slots available."),
                    false
                );
            }
        }

        if (!saveData()) {
            player.displayClientMessage(
                    Component.literal("§c[One Slot] Failed to save world settings."),
                    false
            );
        }
    }

    /**
     * Toggle One Slot mode for the given player.
     * @return the new state (true if now enabled, false if now disabled)
     */
    public static boolean toggle(Player player) {
        boolean newState = !getValidity(player);
        setValidity(player, newState);
        return newState;
    }

    /**
     * Get the number of players with One Slot mode enabled.
     */
    public static int getEnabledPlayerCount() {
        ensureDataLoaded();
        return enabledPlayers.size();
    }

    /**
     * Ensure data is loaded.
     */
    private static void ensureDataLoaded() {
        if (!dataLoaded) {
            loadData();
            dataLoaded = true;
        }
    }

    /**
     * Load enabled players data from properties file.
     */
    private static void loadData() {
        File dataFile = getDataFile();
        if (!dataFile.exists()) {
            return;
        }

        enabledPlayers.clear();
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(dataFile)) {
            props.load(fis);

            for (String key : props.stringPropertyNames()) {
                if (key.startsWith("player.") && "true".equals(props.getProperty(key))) {
                    try {
                        String uuidString = key.substring("player.".length());
                        UUID playerId = UUID.fromString(uuidString);
                        enabledPlayers.add(playerId);
                    } catch (IllegalArgumentException e) {
                        // Skip invalid UUIDs
                    }
                }
            }
        } catch (IOException e) {
            // Failed to load, start with empty set
        }
    }

    /**
     * Save enabled players data to properties file.
     */
    private static boolean saveData() {
        File dataFile = getDataFile();

        // Ensure parent directory exists
        File parentDir = dataFile.getParentFile();
        if (parentDir != null) {
            parentDir.mkdirs();
        }

        Properties props = new Properties();
        for (UUID playerId : enabledPlayers) {
            props.setProperty("player." + playerId.toString(), "true");
        }

        try (FileOutputStream fos = new FileOutputStream(dataFile)) {
            props.store(fos, "One Slot Survival - Enabled Players");
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Get the data file.
     */
    private static File getDataFile() {
        File configDir = new File(System.getProperty("user.dir"), "config");
        configDir.mkdirs();
        return new File(configDir, FILE_NAME);
    }
}