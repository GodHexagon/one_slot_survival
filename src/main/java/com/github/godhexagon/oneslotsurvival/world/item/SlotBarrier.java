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
 * Custom barrier item that blocks inventory slots.
 * This item is invisible to players and cannot be used normally.
 */
public class SlotBarrier extends Item {

    public SlotBarrier(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        // Prevent players from using this item
        return InteractionResult.FAIL;
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        // Remove barrier items that somehow become item entities
        if (!entity.level().isClientSide) {
            entity.discard();
        }
        return true; // Prevent further updates
    }
}