package com.github.godhexagon.oneslotsurvival.world.util;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

public class ItemType {
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
