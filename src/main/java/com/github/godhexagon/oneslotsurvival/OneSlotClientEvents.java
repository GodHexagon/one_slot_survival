package com.github.godhexagon.oneslotsurvival;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side event handlers for One Slot Survival mod.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class OneSlotClientEvents {

    private static boolean lastRestrictionState = false;
    private static int debugMessageCooldown = 0;

    /**
     * Handle client tick events for restriction state detection.
     * Phase 3.1: Basic state detection only (no hotbar control yet).
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Check current restriction state
        boolean currentlyRestricted = OneSlotClientManager.isLocalPlayerRestricted();

        // Debug message cooldown
        if (debugMessageCooldown > 0) {
            debugMessageCooldown--;
        }

        // Log state changes for debugging (Phase 3.1 only)
        if (currentlyRestricted != lastRestrictionState) {
            String status = OneSlotClientManager.getDebugStatus();
            mc.player.displayClientMessage(
                Component.literal("§7[Client Debug] " + status),
                true
            );
            lastRestrictionState = currentlyRestricted;
        }

        // Phase 3.2: Enforce main hand selection
        enforceMainHandSelection();
    }

    /**
     * Force hotbar selection to remain at slot 0 (main hand) during client ticks.
     * Phase 3.2: Disable hotbar scrolling by forcing selection to main hand.
     */
    private static void enforceMainHandSelection() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // Only enforce selection if the local player is restricted
        if (OneSlotClientManager.isLocalPlayerRestricted()) {
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

}