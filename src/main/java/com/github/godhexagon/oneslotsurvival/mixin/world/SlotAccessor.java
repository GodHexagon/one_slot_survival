package com.github.godhexagon.oneslotsurvival.mixin.world;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin for Slot class to access protected methods.
 * This allows us to call protected methods like onSwapCraft from our AbstractContainerMenuMixin.
 */
@Mixin(Slot.class)
public interface SlotAccessor {
    /**
     * Accessor for the protected onSwapCraft method.
     * This method is called when an item is swapped using the number keys.
     *
     * @param count The number of items being swapped
     */
    @Invoker("onSwapCraft")
    void invokeOnSwapCraft(int count);
}
