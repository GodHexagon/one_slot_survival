package com.github.godhexagon.oneslotsurvival.world.command;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * One Slot Survival mod のコマンドのイベントハンドラ
 * コマンド登録を処理
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID)
public class CommandEvent {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandRegisterer.register(event.getDispatcher());
    }
}