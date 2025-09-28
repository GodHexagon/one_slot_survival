package com.github.godhexagon.oneslotsurvival;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for One Slot Survival mod.
 * Handles command registration and other game events.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID)
public class OneSlotEvents {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        OneSlotCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only process on server side
        if (event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        if (!OneSlotManager.isEnabled(player)) {
            return;
        }

        // Apply all inventory restrictions
        processInventoryRestrictions(player);
    }

    /**
     * Public method to process all inventory restrictions for a player.
     * Used both by tick events and when initially enabling the mode.
     */
    public static void processInventoryRestrictions(Player player) {
        // Force hotbar selection to slot 0 (main hand)
        forceMainHandSelection(player);

        // Clear items from prohibited slots (1-35)
        clearProhibitedSlots(player);
    }

    /**
     * Force player to always have slot 0 (main hand) selected
     */
    private static void forceMainHandSelection(Player player) {
        Inventory inventory = player.getInventory();
        // Always force selection to slot 0 (main hand)
        inventory.pickSlot(0);
    }

    /**
     * Clear items from prohibited slots (hotbar slots 1-8 and inventory slots 9-35)
     * Items are moved to main hand if possible, otherwise dropped on ground
     */
    private static void clearProhibitedSlots(Player player) {
        Inventory inventory = player.getInventory();

        // Check slots 1-35 (prohibited slots)
        for (int slot = 1; slot < 36; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (!item.isEmpty()) {
                // Try to move item to main hand (slot 0)
                if (!moveItemToMainHand(player, item, slot)) {
                    // Drop item on ground using vanilla API
                    player.drop(item, false);
                    inventory.setItem(slot, ItemStack.EMPTY);
                }
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
            inventory.setItem(sourceSlot, ItemStack.EMPTY);
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

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // Currently no cleanup needed for player logout
        // This is kept for future expansion
    }
}