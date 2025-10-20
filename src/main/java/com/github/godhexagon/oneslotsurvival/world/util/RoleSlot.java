package com.github.godhexagon.oneslotsurvival.world.util;

import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RoleSlot {
    /**
     * ロールスロットルールにおいて、新しいアイテムの割り当てとして適切か判定します。
     *
     * @param item 新しいアイテム。
     * @param inventoryIndex itemが格納される予定のinventory index。
     * @param player インベントリーの所有者。
     * @return trueのとき適切（配置可能）。
     */
    public static boolean isEligibleItemForRoledPlayer(ItemStack item, int inventoryIndex, Player player) {
        if (InventoryDefinition.isDisableSlot(inventoryIndex)) {
            return item.is(ModItems.SLOT_BARRIER.get());
        }

        if (InventoryDefinition.isRoleSlot(inventoryIndex)) {
            // 将来的に使用します。
            int roleSlotIndex = inventoryIndex - 1;

            return RoleSlot.isPickaxe(item) || item.isEmpty();
        }

        return !item.is(ModItems.SLOT_BARRIER.get());
    }
    /**
     * つるはしかどうかを判定
     *
     * @param item 判定対象のアイテムスタック
     * @return true の場合、つるはし
     */
    public static boolean isPickaxe(ItemStack item) {
        if (item.isEmpty()) {
            return false;
        }
        return item.is(ItemTags.PICKAXES);
    }
}
