package com.github.godhexagon.oneslotsurvival.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * カスタムインベントリ画面
 * 段階的な実装：
 * - ステップ1: 基本的な継承 ✓
 * - ステップ2: 背景画像のカスタマイズ（実装中）
 * - ステップ3: スロット配置の変更（中央下部に1スロット）
 */
@OnlyIn(Dist.CLIENT)
public class CustomInventoryScreen extends InventoryScreen {

    // カスタム背景画像のパス
    private static final ResourceLocation CUSTOM_INVENTORY_LOCATION =
        ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "textures/gui/container/custom_inventory.png");

    // 背景のサイズ（後で調整可能）
    private static final int CUSTOM_IMAGE_WIDTH = 176;
    private static final int CUSTOM_IMAGE_HEIGHT = 166;

    // メインハンドのスロットインデックス（ホットバーの最初）
    private static final int MAIN_HAND_SLOT_INDEX = 36;

    // スロットを中央下部に配置する座標（画面中央からの相対位置）
    private static final int SLOT_CENTER_OFFSET_X = 80; // 中央（176/2 = 88）の少し左
    private static final int SLOT_CENTER_OFFSET_Y = 142; // 下部

    public CustomInventoryScreen(Player player) {
        super(player);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        // カスタム背景を描画
        // 画像が存在しない場合はバニラの背景にフォールバック
        try {
            graphics.blit(RenderPipelines.GUI_TEXTURED, CUSTOM_INVENTORY_LOCATION,
                x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        } catch (Exception e) {
            // フォールバック: バニラの背景を使用
            super.renderBg(graphics, partialTick, mouseX, mouseY);
            return;
        }

        // プレイヤーのレンダリング（バニラと同じ位置）
        renderEntityInInventoryFollowsMouse(graphics, x + 26, y + 8, x + 75, y + 78, 30, 0.0625F, mouseX, mouseY, this.minecraft.player);
    }

    @Override
    protected void renderSlots(GuiGraphics graphics) {
        // ステップ1: メインハンドのスロットのみを描画（元の位置）
        // TODO: 次のステップで座標を中央下部に調整
        if (this.menu.slots.size() > MAIN_HAND_SLOT_INDEX) {
            Slot mainHandSlot = this.menu.slots.get(MAIN_HAND_SLOT_INDEX);
            if (mainHandSlot.isActive()) {
                this.renderSlot(graphics, mainHandSlot);
            }
        }
        // 他のスロットは描画しない（これだけで視覚的な変化が確認できる）
        // TODO: クラフトスロット、防具スロット、オフハンドが表示されない。特定のスロットだけ拒否する実装のほうが良いかも。
        // クリエイティブインベントリーは
    }
}
