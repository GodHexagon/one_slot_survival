package com.github.godhexagon.oneslotsurvival;

import net.minecraft.world.entity.player.Player;
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

        // Check for inventory changes and notify about invalid items
        InventoryMonitor.checkInventoryChange(player);
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        // Clean up stored inventory state when player disconnects
        InventoryMonitor.clearPlayerState(event.getEntity().getUUID());
    }
}