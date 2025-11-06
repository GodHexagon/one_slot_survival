package com.github.godhexagon.oneslotsurvival.rule.inventory;

import com.github.godhexagon.oneslotsurvival.object.item.ModItems;

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
            if (!SlotRestriction.isEligibleItemForRoledPlayer(inventory.getItem(i), i, player)) {
                return true;
            }
        }

        return false;
    }

    public static void fillUp(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack item = inventory.getItem(i);

            // まず変更が必要なスロットかどうかを判定
            if (!SlotRestriction.isEligibleItemForRoledPlayer(item, i, player)) {
                // 無効化スロットにはスロットバリアを入れるべき
                if (InventoryDefinition.isDisableSlot(i)) {
                    player.drop(item, false);
                    inventory.setItem(i, new ItemStack(ModItems.SLOT_BARRIER.get()));
                    continue;
                }

                // ロールスロットは空にすべき
                if (InventoryDefinition.isRoleSlot(i)) {
                    player.drop(item, false);
                    inventory.setItem(i, ItemStack.EMPTY);
                    continue;
                }
                
                // それ以外の場合は、不適切なスロットバリアの場合のはずだけど、念のため検証してから削除
                if (item.is(ModItems.SLOT_BARRIER.get())) {
                    inventory.setItem(i, ItemStack.EMPTY);
                }
            }
        }
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

    public static void clean(ServerPlayer player) {
        Inventory inventory = player.getInventory();

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(ModItems.SLOT_BARRIER.get())) {
                inventory.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
