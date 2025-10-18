package com.github.godhexagon.oneslotsurvival.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.item.ItemEntity;

/**
 * インベントリスロットをブロックするカスタムバリアアイテム
 * このアイテムは、対象プレイヤーから見て使用できない
 */
public class SlotBarrier extends Item {

    public SlotBarrier(Properties properties) {
        super(properties);
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        // 何らかの理由でアイテムエンティティになったバリアアイテムを削除
        if (!entity.level().isClientSide) {
            entity.discard();
        }
        return true; // 以降の更新を防ぐ
    }
}