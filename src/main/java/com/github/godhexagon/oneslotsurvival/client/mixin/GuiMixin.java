package com.github.godhexagon.oneslotsurvival.client.mixin;

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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ここでは、ホットバーレンダリングを改変する
 * One Slot モードが有効なプレイヤーに対して、画面中央に単一スロットのホットバーのみをレンダリング
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    // スプライト定数 - ほとんどはバニラを使用、ホットバー背景のみカスタム
    @Unique
    private static final ResourceLocation CUSTOM_HOTBAR_SPRITE = ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "hud/hotbar");
    @Shadow
    @Final
    private static ResourceLocation HOTBAR_SELECTION_SPRITE;
    @Shadow
    @Final
    private static ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE;
    @Shadow
    @Final
    private static ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE;

    @Shadow
    public abstract void renderSlot(GuiGraphics p_283283_, int p_283213_, int p_281301_, DeltaTracker p_344149_, Player p_283644_, ItemStack p_283317_, int p_283261_);

    @Shadow
    @javax.annotation.Nullable
    public abstract Player getCameraPlayer();

    /**
     * mod が有効な場合、バニラのホットバーレンダリングをカスタムの単一スロットホットバーに置き換え
     */
    @Inject(method = "renderItemHotbar", at = @At("HEAD"), cancellable = true)
    private void onRenderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Player player = this.minecraft.player;

        // このプレイヤーに対して mod が有効な場合のみカスタムレンダリングを適用
        if (player == null || !PlayerModValidity.isEffective(player)) {
            return;
        }

        // バニラレンダリングをキャンセル
        ci.cancel();

        // カスタム単一スロットホットバーをレンダリング
        this.one_slot_survival$renderSingleSlotHotbar(guiGraphics, deltaTracker);
    }

    /**
     * 画面中央にカスタム単一スロットホットバーをレンダリング
     * バニラ背景を使用するが、最初のスロットアイテムのみをレンダリング
     * スロット 0 が中央に表示されるようにホットバーを左にシフト
     */
    @Unique
    private void one_slot_survival$renderSingleSlotHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }

        ItemStack itemstack = player.getOffhandItem();
        HumanoidArm humanoidarm = player.getMainArm().getOpposite();
        int centerX = guiGraphics.guiWidth() / 2;
        int bottomY = guiGraphics.guiHeight() - 22;

        // スロット 0（左端）が中央に表示されるようにホットバーを右に 80 ピクセルシフト
        // 各スロットの幅は 20 ピクセル、4 スロット右にシフト = 80 ピクセル
        int offsetX = 80;
        int hotbarX = centerX - 91 + offsetX;

        // カスタムホットバー背景（幅 182 ピクセル）を右にシフトしてレンダリング
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, CUSTOM_HOTBAR_SPRITE, hotbarX, bottomY, 182, 22);

        // スロット 0 の位置（現在は中央）にバニラ選択オーバーレイをレンダリング
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, hotbarX - 1, bottomY - 1, 24, 23);

        // オフハンドスロットが存在する場合、バニラオフハンドスロットをレンダリング
        if (!itemstack.isEmpty()) {
            if (humanoidarm == HumanoidArm.LEFT) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, hotbarX - 29, bottomY - 1, 29, 24);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_SPRITE, hotbarX + 182, bottomY - 1, 29, 24);
            }
        }

        // 最初のスロットアイテム（スロット 0）のみをレンダリング - 現在は中央
        int itemX = hotbarX + 2; // スロット 0 アイテムの位置
        int itemY = guiGraphics.guiHeight() - 16 - 3;
        ItemStack selectedItem = player.getInventory().getItem(0); // 常にスロット 0 をレンダリング
        this.renderSlot(guiGraphics, itemX, itemY, deltaTracker, player, selectedItem, 1);

        // オフハンドアイテムが存在する場合はレンダリング
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
