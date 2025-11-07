package com.github.godhexagon.oneslotsurvival.rule.inventory;

import java.util.Optional;

import com.github.godhexagon.oneslotsurvival.object.item.ModItems;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.SlotRegistry;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SlotRestriction {

    /**
     * プレイヤーのレベルをもとにそのスロットがアンロックされたロールするっとかどうかを返します。
     * 
     * @param inventoryIndex inventory index。
     * @param player インベントリーの所有者。
     * @return ロールスロットのうち、アンロックされたものであるとき、true。
     */
    public static boolean unlockedRoleSlot(int inventoryIndex, Player player) {
        return InventoryDefinition.isRoleSlot(inventoryIndex) && InventoryDefinition.getRoleSlotIndex(inventoryIndex) < RoleLeveledUpTimes.getMain(player);
    }

    /**
     * ロールスロットルールにおいて、新しいアイテムの割り当てとして適切か判定します。
     *
     * @param item 新しいアイテム。
     * @param inventoryIndex itemが格納される予定のinventory index。
     * @param player インベントリーの所有者。
     * @return trueのとき適切（配置可能）。
     */
    public static boolean isEligibleItemForRoledPlayer(ItemStack item, int inventoryIndex, Player player) {
        // 禁止スロット（スロットバリアが入っているべき）
        if (InventoryDefinition.isDisableSlot(inventoryIndex)) {
            return item.is(ModItems.SLOT_BARRIER.get());
        }

        // ロールスロット
        if (InventoryDefinition.isRoleSlot(inventoryIndex)) {
            int roleSlotIndex = InventoryDefinition.getRoleSlotIndex(inventoryIndex);

            // すでに解放済みのスロットかどうか
            if (unlockedRoleSlot(inventoryIndex, player)) {
                // 新しいSlotRegistryを使用してアイテムタグを取得
                Optional<TagKey<Item>> allowedTag = SlotRegistry.getAllowedItemTag(player, roleSlotIndex);

                if (allowedTag.isPresent()) {
                    return item.isEmpty() || item.is(allowedTag.get());
                } else {
                    // ロールスロットを持たないロールの場合は空のみ許可
                    return item.isEmpty();
                }
            } else {
                // 未解放スロットは空のみ許可
                return item.isEmpty();
            }
        }

        // 特に制限がないスロット
        return !item.is(ModItems.SLOT_BARRIER.get());
    }
}
