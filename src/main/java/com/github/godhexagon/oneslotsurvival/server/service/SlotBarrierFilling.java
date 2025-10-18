package com.github.godhexagon.oneslotsurvival.server.service;

import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * スロットバリアをプレイヤーに配布する処理の集合。
 */
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
     * スロットにバリアアイテムが必要かどうかをチェック
     * スロット 1-35 は禁止（ホットバー 1-8 とインベントリ 9-35）
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
