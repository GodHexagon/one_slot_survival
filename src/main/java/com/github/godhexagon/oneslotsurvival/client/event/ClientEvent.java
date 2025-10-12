package com.github.godhexagon.oneslotsurvival.client.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side event handler for inventory-related events.
 * Handles displaying mod validity status when player opens their inventory.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, value = Dist.CLIENT)
public class ClientEvent {

    /**
     * Called when any screen is about to open on the client.
     * If it's the player's inventory screen, query mod validity from server.
     *
     * @param event the screen opening event
     */
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // Check if the opening screen is the player's inventory
        if (event.getNewScreen() instanceof InventoryScreen) {
            Minecraft minecraft = Minecraft.getInstance();

            // Ensure we have a valid client player and connection
            if (minecraft.player != null) {
                boolean enabled = PlayerModValidity.isEnabled(minecraft.player);
                minecraft.player.displayClientMessage(
                    Component.literal("One Slot Survival Validity: " + enabled),
                    false
                );
            }
        }
    }
}