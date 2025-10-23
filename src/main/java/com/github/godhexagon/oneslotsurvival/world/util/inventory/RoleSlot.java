package com.github.godhexagon.oneslotsurvival.world.util.inventory;

import java.util.List;
import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class RoleSlot {
    public static final List<TagKey<Item>> SPECIALTY_ITEM_LINEUP = List.of(
        ItemTags.PICKAXES,
        ItemTags.AXES,
        ItemTags.SHOVELS
    );

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

            return item.is(SPECIALTY_ITEM_LINEUP.get(InventoryDefinition.getRoleSlotIndex(inventoryIndex))) || item.isEmpty();
        }

        return !item.is(ModItems.SLOT_BARRIER.get());
    }
}
