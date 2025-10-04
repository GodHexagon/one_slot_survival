package com.github.godhexagon.oneslotsurvival.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.core.player.modvalidity.WorldModValidity;
import com.github.godhexagon.oneslotsurvival.core.player.slot.InventoryProcess;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Event handler for One Slot Survival mod.
 * Handles command registration and other game events.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeWorldEvents {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        // 対象プレイヤー以外を除外
        if (!WorldModValidity.isEffective(player)) {
            return;
        }

        // Apply all inventory restrictions
        InventoryProcess.processInventoryRestrictions(player);
    }

    @SubscribeEvent
    public static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        Player player = event.getEntity();
        boolean newEffective = WorldModValidity.isEffective(player, event.getNewGameMode());
        boolean previousEffective = WorldModValidity.isEffective(player, event.getCurrentGameMode());

        if(newEffective && !previousEffective) {
            InventoryProcess.processInventoryRestrictions(player);
        } else if (!newEffective && previousEffective) {
            InventoryProcess.clearBarrierItems(player);
        }
    }


}