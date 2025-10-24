package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.util.inventory.SlotRestriction;
import com.github.godhexagon.oneslotsurvival.world.util.player.PlayerModValidity;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to add role slot restrictions to Slot.mayPlace
 * This prevents items from being placed in role slots via quick move or any other method.
 */
@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow
    public abstract int getContainerSlot();

    /**
     * Inject into mayPlace to check role slot eligibility.
     * This is called for all placement attempts including quick move (Shift+Click).
     */
    @Inject(method = "mayPlace", at = @At("RETURN"), cancellable = true)
    private void onMayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        // Only proceed if the vanilla check already returned true
        if (!cir.getReturnValue()) {
            return;
        }

        Slot slot = (Slot)(Object)this;

        // Only check for player inventory slots
        if (slot.container instanceof Inventory inventory) {
            // Only check if the player has the mod enabled
            if (PlayerModValidity.isEffective(inventory.player)) {
                int inventoryIndex = this.getContainerSlot();

                // Check if the item is eligible for this slot
                if (!SlotRestriction.isEligibleItemForRoledPlayer(stack, inventoryIndex, inventory.player)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}