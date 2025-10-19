package com.github.godhexagon.oneslotsurvival.world.util;

import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * スロットバリアをプレイヤーに配布する処理の集合。
 */
public class SlotBarrierFilling {
    public static void fillUp(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        // TODO: 改変されていないメソッドを利用する必要あり
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);

            // 無効化スロットにはスロットバリアを入れるべき
            if (InventoryDefinition.isDisableSlot(i)) {
                player.drop(item, false);
                inventory.setItem(i, new ItemStack(ModItems.SLOT_BARRIER.get()));
                continue;
            }

            // ロールスロットは基本的に何もしなくていいが、不適切アイテムだっときは空にすべき
            if (InventoryDefinition.isRoleSlot(i) && !ItemType.isPickAxe(item)) {
                player.drop(item, false);
                inventory.setItem(i, ItemStack.EMPTY);
                continue;
            }

            // それ以外の場合にスロットバリアがあったら消す
            if (item.is(ModItems.SLOT_BARRIER.get())) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    public static void clean(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(ModItems.SLOT_BARRIER.get())) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
