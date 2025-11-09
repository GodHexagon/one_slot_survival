package com.github.godhexagon.oneslotsurvival.mixin.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

import com.github.godhexagon.oneslotsurvival.rule.inventory.SlotRestriction;
import com.github.godhexagon.oneslotsurvival.rule.player.PlayerModValidity;

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
    @Unique    
    private static final ResourceLocation HOTBAR_END_SPRITE = ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "hud/hotbar_end");
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
     * GuiGraphicsのinnerBlitメソッドにアクセスするためのヘルパー
     * GuiGraphicsは直接Mixinできないため、リフレクションを使用
     */
    @Unique
    private void one_slot_survival$innerBlit(
        GuiGraphics guiGraphics,
        ResourceLocation texture,
        int x0, int x1,
        int y0, int y1,
        float u0, float u1,
        float v0, float v1
    ) {
        try {
            // GuiGraphics.innerBlit()を呼び出す
            java.lang.reflect.Method method = GuiGraphics.class.getDeclaredMethod(
                "innerBlit",
                com.mojang.blaze3d.pipeline.RenderPipeline.class,
                ResourceLocation.class,
                int.class, int.class, int.class, int.class,
                float.class, float.class, float.class, float.class,
                int.class
            );
            method.setAccessible(true);
            method.invoke(
                guiGraphics,
                RenderPipelines.GUI_TEXTURED,
                texture,
                x0, x1, y0, y1,
                u0, u1, v0, v1,
                -1 // 白色（色調整なし）
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to call GuiGraphics.innerBlit()", e);
        }
    }

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

        // 解放されたスロット数をカウント
        // 最初のスロットはメインハンドスロットだから必ずある(1 +)
        int slot_count = 1 + SlotRestriction.getTotalRoleSlotCount(player);

        // 背景幅を動的計算: 左右輪郭1px + スロット20px * slot_count + 右輪郭1px = 2 + 20 * slot_count
        int bgWidth = 2 + 20 * slot_count;

        // 中央揃え: 背景の中心が画面中央に来るように配置
        int hotbarX = centerX - bgWidth / 2;

        // カスタムホットバー背景を部分描画（左側のみトリミング）
        TextureAtlasSprite sprite = this.minecraft.getGuiSprites().getSprite(CUSTOM_HOTBAR_SPRITE);
        float u0 = sprite.getU0();
        float u1 = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * bgWidth / 182.0f; // 182pxが元の幅
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        // innerBlitを使って部分描画
        this.one_slot_survival$innerBlit(
            guiGraphics,
            sprite.atlasLocation(),
            hotbarX, hotbarX + bgWidth,
            bottomY, bottomY + 22,
            u0, u1, v0, v1
        );

        // ホットバーの右側の輪郭線がないので、これを上書きで表示
        // HOTBAR_END_SPRITEは横幅が3px、高さがホットバー背景と同じ
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_END_SPRITE, hotbarX + bgWidth - 3, bottomY, 3, 22);

        // 選択スロットのオーバーレイを描画（選択されたスロットが解放済みの場合のみ）
        int selectedSlot = player.getInventory().getSelectedSlot();
        if (selectedSlot >= 0 && selectedSlot < slot_count) {
            int selectionX = hotbarX - 1 + selectedSlot * 20;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, selectionX, bottomY - 1, 24, 23);
        }

        // オフハンドスロットの背景を描画（アイテムがある場合のみ）
        if (!itemstack.isEmpty()) {
            if (humanoidarm == HumanoidArm.LEFT) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, hotbarX - 29, bottomY - 1, 29, 24);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_SPRITE, hotbarX + bgWidth, bottomY - 1, 29, 24);
            }
        }

        // 解放されたスロットのアイテムのみ描画
        int itemY = guiGraphics.guiHeight() - 16 - 3;
        for (int i = 0; i < slot_count; i++) {
            int itemX = hotbarX + i * 20 + 3; // バニラの間隔: 20px per slot
            ItemStack selectedItem = player.getInventory().getItem(i);
            this.renderSlot(guiGraphics, itemX, itemY, deltaTracker, player, selectedItem, 1);
        }

        // オフハンドアイテムを描画（存在する場合）
        if (!itemstack.isEmpty()) {
            int offhandY = guiGraphics.guiHeight() - 16 - 3;
            if (humanoidarm == HumanoidArm.LEFT) {
                this.renderSlot(guiGraphics, hotbarX - 26, offhandY, deltaTracker, player, itemstack, 2);
            } else {
                this.renderSlot(guiGraphics, hotbarX + bgWidth + 10, offhandY, deltaTracker, player, itemstack, 2);
            }
        }
    }
}
