package com.github.godhexagon.oneslotsurvival.client.gui;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 制限付きインベントリ画面
 *
 * <p>このクラスは {@link net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen}
 * の設計パターンをコピーして実装されています。</p>
 *
 * <h2>CreativeModeInventoryScreen との類似点</h2>
 * <ul>
 *   <li>元のスロットを {@code originalSlots} にバックアップ</li>
 *   <li>{@code menu.slots} をクリアして再構築</li>
 *   <li>SlotWrapper でラップして表示位置と動作を変更</li>
 *   <li>画面を閉じるときに元のスロット配置に復元</li>
 * </ul>
 *
 * <h2>現在の実装機能</h2>
 * <p><strong>制限対象（画面外に配置して無効化）：</strong></p>
 * <ul>
 *   <li>インベントリ 27スロット（index 9-35）</li>
 *   <li>ホットバー 8スロット（index 37-44、メインハンド36以外）</li>
 * </ul>
 *
 * <p><strong>表示対象（通常通り動作）：</strong></p>
 * <ul>
 *   <li>メインハンドスロット（index 36）</li>
 *   <li>クラフトスロット（2x2）</li>
 *   <li>防具スロット（ヘルメット、チェストプレート、レギンス、ブーツ）</li>
 *   <li>オフハンドスロット</li>
 * </ul>
 *
 * <p><strong>実装方法：</strong></p>
 * <ul>
 *   <li>制限スロットを画面外（-2000, -2000）に配置</li>
 *   <li>{@link RestrictedSlotWrapper} でラップして操作を完全に防ぐ</li>
 * </ul>
 *
 * <h2>今後追加できる機能（CreativeModeInventoryScreen を参考）</h2>
 * <ul>
 *   <li>カスタムスロット配置（メインハンドを中央下部に配置など）</li>
 *   <li>カスタム背景画像の描画</li>
 *   <li>独自のボタン・ウィジェット追加</li>
 *   <li>タブシステム（複数の画面モード）</li>
 *   <li>検索機能</li>
 *   <li>スクロール機能</li>
 *   <li>ロールスロットなどの専用スロット追加</li>
 *   <li>他のコンテナ（チェスト、クラフトテーブルなど）への同様の制限適用</li>
 * </ul>
 *
 * @see net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen
 * @see RestrictedSlotWrapper
 */
@OnlyIn(Dist.CLIENT)
public class RestrictedInventoryScreen extends InventoryScreen {
    private static final ResourceLocation CUSTOM_INVENTORY_LOCATION =
            ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "textures/gui/container/custom_inventory.png");

    // 元のスロットのバックアップ（画面を閉じるときに復元用、将来的に必要になる可能性がある）
    @Nullable
    private List<Slot> originalSlots = null;

    public RestrictedInventoryScreen(Player player) {
        super(player);
    }

    @Override
    protected void init() {
        super.init();

        // 元のスロットをバックアップ（一度だけ）
        if (this.originalSlots == null) {
            this.originalSlots = ImmutableList.copyOf(this.menu.slots);
        }

        // スロットリストをクリアして再構築
        this.menu.slots.clear();

        // 各スロットを再配置
        for (int i = 0; i < this.originalSlots.size(); i++) {
            Slot originalSlot = this.originalSlots.get(i);
            int x = originalSlot.x;
            int y = originalSlot.y;
            boolean restricted = false;

            // 制限対象のスロットを判定（menu.slots内のインデックス i を使用）
            if (isRestrictedSlot(i)) {
                // 画面外に配置
                x = -2000;
                y = -2000;
                restricted = true;
            }

            // RestrictedSlotWrapperでラップして追加
            // 重要: スロットのindexには i を渡す（menu.slots内の位置）
            RestrictedSlotWrapper wrapper = new RestrictedSlotWrapper(originalSlot, i, x, y, restricted);
            this.menu.slots.add(wrapper);
        }
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.slots.clear();
        this.menu.slots.addAll(this.originalSlots);
        this.originalSlots = null;
    }

    /**
     * 制限対象のスロットかどうかを判定
     *
     * @param index スロットインデックス
     * @return true の場合、制限対象
     */
    private boolean isRestrictedSlot(int index) {
        // インベントリスロット（9-35）を制限
        if (index >= 9 && index <= 35) {
            return true;
        }

        // ホットバースロット（37-44、メインハンド36以外）を制限
        if (index >= 37 && index <= 44) {
            return true;
        }

        // その他のスロット（クラフト、防具、オフハンド、メインハンド）は制限しない
        return false;
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
}
