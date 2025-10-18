package com.github.godhexagon.oneslotsurvival.mixin.client;

import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to modify hotbar rendering.
 * For players with One Slot mode enabled, renders only a single slot hotbar centered on screen.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    // Sprite constants - using vanilla for most, custom only for hotbar background
    private static final ResourceLocation CUSTOM_HOTBAR_SPRITE = ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");

    @Shadow
    protected abstract void renderSlot(GuiGraphics p_283283_, int p_283213_, int p_281301_, DeltaTracker p_344149_, Player p_283644_, ItemStack p_283317_, int p_283261_);

    @Shadow
    @javax.annotation.Nullable
    protected abstract Player getCameraPlayer();

    /**
     * Replaces the vanilla hotbar rendering with a custom single-slot hotbar when mod is effective.
     */
    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Player player = this.minecraft.player;

        // Only apply custom rendering if the mod is enabled for this player
        if (player == null || !PlayerModValidity.isEffective(player)) {
            return;
        }

        // Cancel vanilla rendering
        ci.cancel();

        // Render custom single-slot hotbar
        this.renderSingleSlotHotbar(guiGraphics, deltaTracker);
    }

    /**
     * Renders a custom single-slot hotbar centered on screen.
     * Uses vanilla background but only renders the first slot item.
     * The hotbar is shifted left so that slot 0 appears in the center.
     */
    private void renderSingleSlotHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }

        ItemStack itemstack = player.getOffhandItem();
        HumanoidArm humanoidarm = player.getMainArm().getOpposite();
        int centerX = guiGraphics.guiWidth() / 2;
        int bottomY = guiGraphics.guiHeight() - 22;

        // Shift hotbar RIGHT by 80 pixels so that slot 0 (leftmost) appears in center
        // Each slot is 20 pixels wide, shifting right 4 slots = 80 pixels
        int offsetX = 80;
        int hotbarX = centerX - 91 + offsetX;

        // Render custom hotbar background (182 pixels wide), shifted right
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CUSTOM_HOTBAR_SPRITE, hotbarX, bottomY, 182, 22);

        // Render vanilla selection overlay based on selected slot (0-3)
        // Selection sprite is -1 from hotbar position and moves 20px per slot
        int selectedSlot = player.getInventory().getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot <= 3) {
            int selectionX = hotbarX - 1 + selectedSlot * 20;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, selectionX, bottomY - 1, 24, 23);
        }

        // Render vanilla offhand slot if present
        if (!itemstack.isEmpty()) {
            if (humanoidarm == HumanoidArm.LEFT) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, hotbarX - 29, bottomY - 1, 29, 24);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_SPRITE, hotbarX + 182, bottomY - 1, 29, 24);
            }
        }

        // Render items (0-3)
        // Using vanilla spacing: each slot is 20 pixels apart with +2 base offset
        int itemY = guiGraphics.guiHeight() - 16 - 3;
        // 常に0 ~ 3を表示
        for (int i = 0; i <= 3; i++) {
            int itemX = hotbarX + i * 20 + 2; // Vanilla spacing: 20px per slot, +2 offset
            ItemStack selectedItem = player.getInventory().getItem(i);
            this.renderSlot(guiGraphics, itemX, itemY, deltaTracker, player, selectedItem, 1);
        }

        // Render offhand item if present
        if (!itemstack.isEmpty()) {
            int offhandY = guiGraphics.guiHeight() - 16 - 3;
            if (humanoidarm == HumanoidArm.LEFT) {
                this.renderSlot(guiGraphics, hotbarX - 26, offhandY, deltaTracker, player, itemstack, 2);
            } else {
                this.renderSlot(guiGraphics, hotbarX + 182 + 10, offhandY, deltaTracker, player, itemstack, 2);
            }
        }
    }
}
