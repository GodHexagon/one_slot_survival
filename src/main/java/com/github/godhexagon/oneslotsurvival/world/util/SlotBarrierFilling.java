package com.github.godhexagon.oneslotsurvival.world.util;

import net.minecraft.server.level.ServerPlayer;
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
            if (!RoleSlot.isEligibleItemForRoledPlayer(inventory.getItem(i), i, player)) {
                return true;
            }
        }

        return false;
    }

    public static void fillUp(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);

            // 無効化スロットは空であるべき
            if (InventoryDefinition.isDisableSlot(i)) {
                player.drop(item, false);
                inventory.setItem(i, ItemStack.EMPTY);
                continue;
            }

            // ロールスロットは基本的に何もしなくていいが、不適切アイテムだっときは空にすべき
            if (InventoryDefinition.isRoleSlot(i) && !RoleSlot.isPickaxe(item)) {
                player.drop(item, false);
                inventory.setItem(i, ItemStack.EMPTY);
                continue;
            }
        }
    }
}
