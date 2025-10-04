package com.github.godhexagon.oneslotsurvival.core.player.modvalidity;

import com.github.godhexagon.oneslotsurvival.core.player.slot.InventoryProcess;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side manager for detecting One Slot mode status.
 * Uses barrier item detection to infer server-side restriction state.
 */
@OnlyIn(Dist.CLIENT)
public class ClientModValidity {

    /**
     * Check if the local player is currently restricted by One Slot mode.
     * Detection method: Look for slot_barrier items in prohibited slots (1-35).
     */
    public static boolean isLocalPlayerEnabled() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }

        return InventoryProcess.isProcessedInventoryRestrictions(mc.player);
    }
}