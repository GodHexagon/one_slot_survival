package com.github.godhexagon.oneslotsurvival.world.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.ItemEntity;
import org.jetbrains.annotations.NotNull;

/**
 * インベントリスロットをブロックするカスタムバリアアイテム
 * このアイテムは、対象プレイヤーから見て使用できない
 */
public class SlotBarrier extends Item {

    public SlotBarrier(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        // プレイヤーがこのアイテムを使用するのを防ぐ
        return InteractionResult.FAIL;
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