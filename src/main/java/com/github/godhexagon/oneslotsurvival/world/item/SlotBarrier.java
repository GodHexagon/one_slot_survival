package com.github.godhexagon.oneslotsurvival.world.item;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Custom barrier item that blocks inventory slots.
 * This item is invisible to players and cannot be used normally.
 * When held, it behaves exactly like an empty hand (no mining speed bonus, no attack damage).
 */
public class SlotBarrier extends Item {

    public SlotBarrier(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                // Return EMPTY pose to make the player look like they're holding nothing
                return HumanoidModel.ArmPose.EMPTY;
            }
        });
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

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull BlockState state) {
        // Return 1.0f to behave like an empty hand (no mining speed bonus)
        return 1.0f;
    }

    @Override
    public boolean isCorrectToolForDrops(@NotNull ItemStack stack, @NotNull BlockState state) {
        // Never considered the correct tool, just like an empty hand
        return false;
    }
}