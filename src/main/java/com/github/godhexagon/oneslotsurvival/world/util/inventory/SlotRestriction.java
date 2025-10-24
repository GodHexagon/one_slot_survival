package com.github.godhexagon.oneslotsurvival.world.util.inventory;

import java.util.List;
import java.util.Map;

import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import com.github.godhexagon.oneslotsurvival.world.item.ModTags;
import com.github.godhexagon.oneslotsurvival.world.util.role.MainRole;
import com.github.godhexagon.oneslotsurvival.world.util.role.RoleManager;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SlotRestriction {
    private static final Map<MainRole, List<TagKey<Item>>> SPECIALTY_ITEM_LINEUP = Map.of(
        MainRole.MINER, List.of(ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.AXES),
        MainRole.WARRIOR, List.of(ItemTags.SWORDS, ModTags.Items.WARRIOR_DEFENSIVE, ModTags.Items.WARRIOR_SPECIAL_WEAPONS)
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
            if (RoleManager.hasRole(player)) {
                MainRole role = RoleManager.getRole(player);
                int roleIndex = InventoryDefinition.getRoleSlotIndex(inventoryIndex);
                return item.isEmpty() || item.is(SPECIALTY_ITEM_LINEUP.get(role).get(roleIndex));
            } else {
                return item.isEmpty();
            }
        }

        return !item.is(ModItems.SLOT_BARRIER.get());
    }
}
