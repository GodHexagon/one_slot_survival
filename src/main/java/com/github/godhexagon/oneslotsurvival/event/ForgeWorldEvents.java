package com.github.godhexagon.oneslotsurvival.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.core.player.modvalidity.Configuration;
import com.github.godhexagon.oneslotsurvival.core.player.slot.InventoryProcess;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Event handler for One Slot Survival mod.
 * Handles command registration and other game events.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeWorldEvents {

    // Track which players had restrictions processed in the previous tick
    private static final Map<UUID, Boolean> previousTickRestrictions = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only process on server side
        if (event.player.level().isClientSide) {
            return;
        }

        // 対象プレイヤーでなくなった時だけ、バリアアイテムをインベントリから削除する
        Player player = event.player;
        UUID playerId = player.getUUID();
        boolean currentlyEnabled = Configuration.isEnabled(player);
        Boolean wasEnabledLastTick = previousTickRestrictions.get(playerId);

        // Check if restrictions stopped being processed (transition from enabled to disabled)
        if (wasEnabledLastTick != null && wasEnabledLastTick && !currentlyEnabled) {
            // Clear barrier items when restrictions stop
            InventoryProcess.clearBarrierItems(player);
        }

        // Update tracking for this tick
        previousTickRestrictions.put(playerId, currentlyEnabled);

        if (!currentlyEnabled) {
            return;
        }

        // Apply all inventory restrictions
        InventoryProcess.processInventoryRestrictions(player);
    }

}