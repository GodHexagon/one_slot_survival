package com.github.godhexagon.oneslotsurvival.mixin.world;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Accessor mixin to expose protected methods in the Slot class.
 */
@Mixin(Slot.class)
public interface SlotAccessor {
    /**
     * Invokes the protected onSwapCraft method.
     * This method is called when items are swapped via number keys or offhand key.
     * @param count The count of items being swapped
     */
    @Invoker("onSwapCraft")
    void invokeOnSwapCraft(int count);
}
