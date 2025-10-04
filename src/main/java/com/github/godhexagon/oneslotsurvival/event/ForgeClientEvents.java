package com.github.godhexagon.oneslotsurvival.event;

import com.github.godhexagon.oneslotsurvival.core.player.hotbar.HotbarSelection;
import com.github.godhexagon.oneslotsurvival.core.player.modvalidity.ClientModValidity;
import com.github.godhexagon.oneslotsurvival.core.player.slot.SlotBarrier;
import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side event handlers for One Slot Survival mod.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ForgeClientEvents {

    /**
     * Handle client tick events for hotbar control.
     * Phase 3.2: Enforce main hand selection.
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        // Only enforce selection if the local player is restricted
        if (!ClientModValidity.isLocalPlayerEnabled()) {
            return;
        }

        // Enforce main hand selection
        HotbarSelection.enforceMainHandSelection();
    }

    /**
     * Hide tooltips for barrier items.
     * Phase 3.4.3: Prevent barrier item tooltips from being displayed.
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (SlotBarrier.isBarrierItem(event.getItemStack())) {
            event.getToolTip().clear();
        }
    }

}