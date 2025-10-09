package com.github.godhexagon.oneslotsurvival.server.command;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for One Slot Survival mod.
 * Handles command registration and other game events.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID)
public class CommandEvent {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandRegisterer.register(event.getDispatcher());
    }
}