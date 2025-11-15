package com.github.godhexagon.oneslotsurvival.rule.inventory;

import java.util.Optional;

import com.github.godhexagon.oneslotsurvival.object.item.ModItems;
import com.github.godhexagon.oneslotsurvival.rule.level.RoleLeveledUpTimes;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleSlot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * プレイヤーのレベルとロールを考慮して、Inventoryのコンテナインデックス(Inventory Index)ごとのルールを提供する。
 */
public class SlotRestriction {

    /**
     * Inventoryクラスのインデックスからメインロールの各ロールのロールスロットのインデックスに変換する
     * 
     * @param inventoryIndex Inventoryクラスのインデックス
     * @return ロールスロットのインデックス
     */
    public static int getMainRoleSlotIndex(int inventoryIndex) {
        return InventoryDefinition.getRoleSlotIndex(inventoryIndex);
    }

    /**
     * Inventoryクラスのインデックスからサブロールの各ロールのロールスロットのインデックスに変換する
     * 
     * @param inventoryIndex Inventoryクラスのインデックス
     * @return ロールスロットのインデックス
     */
    public static int getSubRoleSlotIndex(int inventoryIndex, Player player) {
        int mainRoleSlotCount = RoleManager.getMainRole(player).getAvailableRoleSlots(RoleLeveledUpTimes.getMain(player)).size();
        int roleSlotIndex = InventoryDefinition.getRoleSlotIndex(inventoryIndex);
        return roleSlotIndex - mainRoleSlotCount;
    }

    /**
     * プレイヤーのレベルをもとにアンロックされた総ロールスロット数を返します。
     *
     * @param player プレイヤー。
     * @return メインロールとサブロールの合計ロールスロット数。
     */
    public static int getTotalRoleSlotCount(Player player) {
        return RoleManager.getMainRole(player).getAvailableRoleSlots(RoleLeveledUpTimes.getMain(player)).size() +
            RoleManager.getSubRole(player).getAvailableRoleSlots(RoleLeveledUpTimes.getSub(player)).size();
    }

    /**
     * メインロールのロールスロットがアンロックされているか判定します。
     *
     * @param inventoryIndex inventory index。
     * @param player インベントリーの所有者。
     * @return アンロックされているとき、true。
     */
    public static boolean unlockedMainRoleSlot(int inventoryIndex, Player player) {
        return RoleManager.getMainRole(player).getAvailableRoleSlot(SlotRestriction.getMainRoleSlotIndex(inventoryIndex), RoleLeveledUpTimes.getMain(player)).isPresent();
    }

    /**
     * サブロールのロールスロットがアンロックされているか判定します。
     *
     * @param inventoryIndex inventory index。
     * @param player インベントリーの所有者。
     * @return アンロックされているとき、true。
     */
    public static boolean unlockedSubRoleSlot(int inventoryIndex, Player player) {
        return RoleManager.getSubRole(player).getAvailableRoleSlot(getSubRoleSlotIndex(inventoryIndex, player), RoleLeveledUpTimes.getSub(player)).isPresent();
    }

    /**
     * プレイヤーのレベルをもとにそのスロットがアンロックされたロールスロットかどうかを返します。
     * 
     * @param inventoryIndex inventory index。
     * @param player インベントリーの所有者。
     * @return ロールスロットのうち、アンロックされたものであるとき、true。
     */
    public static boolean unlockedRoleSlot(int inventoryIndex, Player player) {
        boolean unlocked = unlockedMainRoleSlot(inventoryIndex, player);
        if (!unlocked) {
            unlocked = unlockedSubRoleSlot(inventoryIndex, player);
        }
        return unlocked;
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
            // メインロールのアイテムタグを取得
            int mainRoleSlotIndex = getMainRoleSlotIndex(inventoryIndex);
            int mainRoleLevelUpTimes = RoleLeveledUpTimes.getMain(player);
            Optional<RoleSlot> roleSlot = RoleManager.getMainRole(player).getAvailableRoleSlot(mainRoleSlotIndex, mainRoleLevelUpTimes);

            if (roleSlot.isPresent()) {
                // 有効なロールスロット
                return item.isEmpty() || item.is(roleSlot.get().itemTag());
            } else {
                // サブロールのアイテムタグを取得
                int subRoleSlotIndex = getSubRoleSlotIndex(inventoryIndex, player);
                int subRoleLevelUpTimes = RoleLeveledUpTimes.getSub(player);
                roleSlot = RoleManager.getSubRole(player).getAvailableRoleSlot(subRoleSlotIndex, subRoleLevelUpTimes);

                if (roleSlot.isPresent()) {
                    // 有効なロールスロット
                    return item.isEmpty() || item.is(roleSlot.get().itemTag());
                } else {
                    // ロールがない、またはスロットが無効な場合、ロールスロットの場合は空のみ許可
                    return item.isEmpty();
                }
            }
        }

        // 特に制限がないスロット
        return !item.is(ModItems.SLOT_BARRIER.get());
    }
}
