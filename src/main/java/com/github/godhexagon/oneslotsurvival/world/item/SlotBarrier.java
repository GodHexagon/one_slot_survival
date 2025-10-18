package com.github.godhexagon.oneslotsurvival.world.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
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
 * In first-person view, displays the player's arm with their skin.
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

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                                    ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
                // Render the player's arm instead of the item
                if (player != null && !player.isInvisible()) {
                    renderPlayerArm(poseStack, player, arm, equipProcess, swingProcess);
                    return true; // Skip other transforms and item rendering
                }
                return false;
            }

            private void renderPlayerArm(PoseStack poseStack, LocalPlayer player, HumanoidArm arm,
                                          float equipProcess, float swingProcess) {
                Minecraft minecraft = Minecraft.getInstance();
                MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
                int light = minecraft.getEntityRenderDispatcher().getPackedLightCoords(player, partialTick);

                boolean isRightHand = arm == HumanoidArm.RIGHT;
                float handSide = isRightHand ? 1.0F : -1.0F;
                float swingSqrt = Mth.sqrt(swingProcess);
                float f2 = -0.3F * Mth.sin(swingSqrt * (float) Math.PI);
                float f3 = 0.4F * Mth.sin(swingSqrt * (float) (Math.PI * 2));
                float f4 = -0.4F * Mth.sin(swingProcess * (float) Math.PI);

                // Apply transformations similar to vanilla empty hand rendering
                poseStack.translate(handSide * (f2 + 0.64000005F), f3 + -0.6F + equipProcess * -0.6F, f4 + -0.71999997F);
                poseStack.mulPose(Axis.YP.rotationDegrees(handSide * 45.0F));
                float f5 = Mth.sin(swingProcess * swingProcess * (float) Math.PI);
                float f6 = Mth.sin(swingSqrt * (float) Math.PI);
                poseStack.mulPose(Axis.YP.rotationDegrees(handSide * f6 * 70.0F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(handSide * f5 * -20.0F));
                poseStack.translate(handSide * -1.0F, 3.6F, 3.5F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(handSide * 120.0F));
                poseStack.mulPose(Axis.XP.rotationDegrees(200.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(handSide * -135.0F));
                poseStack.translate(handSide * 5.6F, 0.0F, 0.0F);

                // Render the actual player arm
                EntityRenderDispatcher renderDispatcher = minecraft.getEntityRenderDispatcher();
                PlayerRenderer playerRenderer = (PlayerRenderer) renderDispatcher.getRenderer(player);
                ResourceLocation skinTexture = player.getSkin().texture();

                if (isRightHand) {
                    playerRenderer.renderRightHand(poseStack, bufferSource, light, skinTexture,
                            player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE));
                } else {
                    playerRenderer.renderLeftHand(poseStack, bufferSource, light, skinTexture,
                            player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE));
                }

                bufferSource.endBatch();
            }

            private float partialTick = 0.0F;
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