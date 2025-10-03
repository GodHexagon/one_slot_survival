package com.github.godhexagon.oneslotsurvival.core.player.modvalidity;

import com.github.godhexagon.oneslotsurvival.object.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side manager for detecting One Slot mode status.
 * Uses barrier item detection to infer server-side restriction state.
 */
@OnlyIn(Dist.CLIENT)
public class ClientState {

    /**
     * Check if the local player is currently restricted by One Slot mode.
     * Detection method: Look for slot_barrier items in prohibited slots (1-35).
     */
    public static boolean isLocalPlayerRestricted() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }

        return hasBarrierItemsInProhibitedSlots(mc.player);
    }

    /**
     * Check if the player has barrier items in any prohibited slots.
     * Prohibited slots: hotbar 1-8 (slots 1-8) + inventory 9-35 (slots 9-35)
     */
    private static boolean hasBarrierItemsInProhibitedSlots(Player player) {
        // Check hotbar slots 1-8 (indices 1-8)
        for (int i = 1; i <= 8; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isSlotBarrierItem(stack)) {
                return true;
            }
        }

        // Check inventory slots 9-35 (indices 9-35)
        for (int i = 9; i <= 35; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isSlotBarrierItem(stack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if the given item stack is a slot barrier item.
     */
    private static boolean isSlotBarrierItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getItem() == ModItems.SLOT_BARRIER.get();
    }
}