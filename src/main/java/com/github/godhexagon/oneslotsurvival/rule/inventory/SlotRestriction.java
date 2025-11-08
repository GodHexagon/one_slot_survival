package com.github.godhexagon.oneslotsurvival.rule.inventory;

import java.util.Optional;

import com.github.godhexagon.oneslotsurvival.object.item.ModItems;
import com.github.godhexagon.oneslotsurvival.rule.role.LevelRewardRegistry;
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
        return LevelRewardRegistry.getAllowedItemTag(player, InventoryDefinition.getRoleSlotIndex(inventoryIndex)).isPresent();
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

            // アイテムタグを取得
            Optional<TagKey<Item>> allowedTag = LevelRewardRegistry.getAllowedItemTag(player, roleSlotIndex);

            if (allowedTag.isPresent()) {
                // 有効なロールスロット
                return item.isEmpty() || item.is(allowedTag.get());
            } else {
                // ロールがない、またはスロットが無効な場合、ロールスロットの場合は空のみ許可
                return item.isEmpty();
            }
        }

        // 特に制限がないスロット
        return !item.is(ModItems.SLOT_BARRIER.get());
    }
}
