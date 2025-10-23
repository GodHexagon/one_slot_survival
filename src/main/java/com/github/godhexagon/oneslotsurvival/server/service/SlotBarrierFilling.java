package com.github.godhexagon.oneslotsurvival.server.service;

import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SlotBarrierFilling {
    public static boolean shouldBeFilledUp(Player player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            boolean exceptedBarrier = shouldHaveBarrier(i);
            boolean foundBarrier = inventory.getItem(i).is(ModItems.SLOT_BARRIER.get());
            if ((!exceptedBarrier && foundBarrier) || (exceptedBarrier && !foundBarrier)) {
                return true;
            }
        }

        return false;
    }

    public static void fillUp(Player player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            boolean exceptedBarrier = shouldHaveBarrier(i);
            boolean foundBarrier = inventory.getItem(i).is(ModItems.SLOT_BARRIER.get());

            if (exceptedBarrier) {
                player.drop(inventory.getItem(i), false);
                inventory.setItem(i, new ItemStack(ModItems.SLOT_BARRIER.get()));
                continue;
            }

            if (foundBarrier) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * Check if a slot should have a barrier item
     * Slots 1-35 are prohibited (hotbar 1-8 and inventory 9-35)
     */
    public static boolean shouldHaveBarrier(int slotId) {
        return slotId >= 1 && slotId <= 35;
    }

    public static boolean shouldBeClean(Player player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(ModItems.SLOT_BARRIER.get())) {
                return true;
            }
        }

        return false;
    }

    public static void clean(Player player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(ModItems.SLOT_BARRIER.get())) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
