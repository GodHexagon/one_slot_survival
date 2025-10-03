package com.github.godhexagon.oneslotsurvival.core.player.slot;

import com.github.godhexagon.oneslotsurvival.object.item.ModItems;
import net.minecraft.world.item.ItemStack;

/**
 * Utility class for creating and managing slot barrier items.
 * Uses a custom MOD item to avoid conflicts with vanilla barrier blocks.
 */
public class BarrierItem {

    /**
     * Creates a barrier ItemStack that acts as a transparent blocker.
     * Uses our custom slot_barrier item to avoid conflicts.
     */
    public static ItemStack createBarrierStack() {
        ItemStack barrier = new ItemStack(ModItems.SLOT_BARRIER.get());
        // Set count to 1 to take up the slot
        barrier.setCount(1);
        return barrier;
    }

    /**
     * Check if an ItemStack is our custom slot barrier item
     */
    public static boolean isBarrierItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        // Check if it's our custom slot barrier item
        return stack.getItem() == ModItems.SLOT_BARRIER.get();
    }

    /**
     * Check if a slot should have a barrier item
     * Slots 1-35 are prohibited (hotbar 1-8 and inventory 9-35)
     */
    public static boolean shouldHaveBarrier(int slot) {
        return slot >= 1 && slot <= 35;
    }
}