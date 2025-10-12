package com.github.godhexagon.oneslotsurvival.client.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.client.gui.RestrictedInventoryScreen;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side event handler for inventory-related events.
 * Handles displaying mod validity status when player opens their inventory.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        enforceMainHandSelection();
    }

    /**
     * Force hotbar selection to remain at slot 0 (main hand) during client ticks.
     */
    private static void enforceMainHandSelection() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Only enforce selection if the local player is restricted
        if (PlayerModValidity.isEffective(mc.player)) {
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

    /**
     * Called when any screen is about to open on the client.
     * If it's the player's inventory screen, replace it with RestrictedInventoryScreen
     * to restrict inventory and hotbar slots.
     *
     * @param event the screen opening event
     */
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // Check if the opening screen is the player's inventory
        // But not already our restricted screen (to avoid infinite loop)
        if (event.getNewScreen() instanceof InventoryScreen &&
            !(event.getNewScreen() instanceof RestrictedInventoryScreen)) {

            Minecraft minecraft = Minecraft.getInstance();

            // Ensure we have a valid client player and connection
            if (minecraft.player != null && PlayerModValidity.isEffective(minecraft.player)) {
                // Replace vanilla InventoryScreen with our RestrictedInventoryScreen
                event.setNewScreen(new RestrictedInventoryScreen(minecraft.player));
            }
        }
    }
}