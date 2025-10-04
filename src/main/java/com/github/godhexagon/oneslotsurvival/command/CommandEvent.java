package com.github.godhexagon.oneslotsurvival.command;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.command.admin.action.PlayerValidityCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommandEvent {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        PlayerValidityCommand.register(event.getDispatcher());
    }

}
