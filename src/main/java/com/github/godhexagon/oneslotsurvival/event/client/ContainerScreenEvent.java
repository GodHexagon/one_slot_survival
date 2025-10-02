package com.github.godhexagon.oneslotsurvival.event.client;

import com.github.godhexagon.oneslotsurvival.core.player.OneSlotClientManager;
import com.github.godhexagon.oneslotsurvival.core.slot.BarrierItem;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ContainerScreenEvent {
    public static void onPlayerInventorySlotClicked(Slot slot, CallbackInfo ci) {
        // Only intercept if the player is restricted
        if (!OneSlotClientManager.isLocalPlayerRestricted()) {
            return;
        }

        // Check if this is a prohibited player inventory slot (1-35)
        if (BarrierItem.shouldHaveBarrier(slot.getSlotIndex())) {
            // Cancel the slot click by cancelling the callback
            ci.cancel();
        }
    }
}
