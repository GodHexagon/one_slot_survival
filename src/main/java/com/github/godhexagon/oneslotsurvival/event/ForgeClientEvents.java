package com.github.godhexagon.oneslotsurvival.event;

import com.github.godhexagon.oneslotsurvival.core.player.slot.BarrierItem;
import com.github.godhexagon.oneslotsurvival.core.player.modvalidity.ClientModValidity;
import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraft.client.Minecraft;
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
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
        if (ClientModValidity.isLocalPlayerRestricted()) {
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
     * Hide tooltips for barrier items.
     * Phase 3.4.3: Prevent barrier item tooltips from being displayed.
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (BarrierItem.isBarrierItem(event.getItemStack())) {
            event.getToolTip().clear();
        }
    }

}