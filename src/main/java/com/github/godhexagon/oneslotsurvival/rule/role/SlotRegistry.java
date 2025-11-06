package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.util.Optional;

/**
 * ロールスロットのアイテム制限を管理するユーティリティクラス
 * <p>
 * このクラスは、プレイヤーのロールとスロットインデックスに基づいて、
 * どのアイテムタグが許可されているかを提供します。
 * MainRoleの機能をラップして、より使いやすいAPIを提供します。
 * </p>
 */
public class SlotRegistry {

    /**
     * 指定されたロールスロットで許可されるアイテムタグを取得
     * <p>
     * プレイヤーのロールとスロットインデックスから、そのスロットで使用可能な
     * アイテムタグを返します。
     * </p>
     *
     * @param player プレイヤー
     * @param roleSlotIndex ロールスロットのインデックス（0から始まる）
     * @return 許可されるアイテムタグ。ロールがない、またはスロットが無効な場合は空
     */
    public static Optional<TagKey<Item>> getAllowedItemTag(Player player, int roleSlotIndex) {
        MainRole role = RoleManager.getRole(player);
        return getAllowedItemTag(role, roleSlotIndex);
    }

    /**
     * 指定されたロールとスロットインデックスで許可されるアイテムタグを取得
     * <p>
     * ロールのアイテムタグリストから、指定されたインデックスのタグを返します。
     * </p>
     *
     * @param role ロール
     * @param roleSlotIndex ロールスロットのインデックス（0から始まる）
     * @return 許可されるアイテムタグ。ロールがスロットを持たない、またはインデックスが範囲外の場合は空
     */
    public static Optional<TagKey<Item>> getAllowedItemTag(MainRole role, int roleSlotIndex) {
        if (!role.hasRoleSlots()) {
            return Optional.empty();
        }

        var tags = role.getSlotItemTags();
        if (roleSlotIndex < 0 || roleSlotIndex >= tags.size()) {
            return Optional.empty();
        }

        return Optional.of(tags.get(roleSlotIndex));
    }

    /**
     * プレイヤーが持つロールスロットの数を取得
     * <p>
     * プレイヤーのロールに基づいて、利用可能なロールスロットの総数を返します。
     * </p>
     *
     * @param player プレイヤー
     * @return ロールスロットの総数（ロールがない場合は0）
     */
    public static int getTotalRoleSlots(Player player) {
        MainRole role = RoleManager.getRole(player);
        return getTotalRoleSlots(role);
    }

    /**
     * 指定されたロールが持つロールスロットの数を取得
     *
     * @param role ロール
     * @return ロールスロットの総数（ロールがスロットを持たない場合は0）
     */
    public static int getTotalRoleSlots(MainRole role) {
        return role.getSlotItemTags().size();
    }
}
