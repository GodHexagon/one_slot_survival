package com.github.godhexagon.oneslotsurvival.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * 制限付きスロットラッパー
 *
 * <p>このクラスは {@link net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper}
 * の設計パターンをコピーして実装されています。</p>
 *
 * <h2>CreativeModeInventoryScreen.SlotWrapper との類似点</h2>
 * <ul>
 *   <li>元のスロットをラップして表示位置と動作を変更</li>
 *   <li>すべてのメソッドを元の target スロットに委譲</li>
 *   <li>必要に応じてメソッドをオーバーライドして動作をカスタマイズ</li>
 * </ul>
 *
 * <h2>現在の実装機能</h2>
 * <ul>
 *   <li>{@code isActive()} を false にしてスロットを無効化（クリック、ツールチップ、描画を防ぐ）</li>
 *   <li>{@code mayPickup()} と {@code mayPlace()} で取得・配置を防ぐ</li>
 *   <li>画面外（-2000, -2000）に配置して視覚的に隠す</li>
 * </ul>
 *
 * <h2>今後追加できる機能（CreativeModeInventoryScreen を参考）</h2>
 * <ul>
 *   <li>スロットごとの個別制御（特定のアイテムのみ許可など）</li>
 *   <li>カスタム背景スプライト（{@code setBackground()}）</li>
 *   <li>カスタムツールチップ表示</li>
 *   <li>ドラッグ＆ドロップの制御</li>
 *   <li>クイックムーブ（Shift+クリック）の制御</li>
 * </ul>
 *
 * @see net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper
 */
@OnlyIn(Dist.CLIENT)
public class RestrictedSlotWrapper extends Slot {
    private final Slot target;
    private final boolean restricted;

    /**
     * @param target 元のスロット
     * @param index スロットインデックス（menu.slots内の位置）
     * @param x 表示X座標（-2000で画面外）
     * @param y 表示Y座標（-2000で画面外）
     * @param restricted true の場合、このスロットを制限する
     */
    public RestrictedSlotWrapper(Slot target, int index, int x, int y, boolean restricted) {
        super(target.container, target.getContainerSlot(), x, y);
        this.target = target;
        this.restricted = restricted;
        // 重要: スロットのindexを正しく設定する
        this.index = index;
    }

    // === 制限機能 ===

    @Override
    public boolean isActive() {
        // 制限されている場合、スロットを無効化
        // これにより、クリック、ツールチップ、描画がすべて防がれる
        return !restricted && target.isActive();
    }

    @Override
    public boolean mayPickup(Player player) {
        // 制限されている場合、アイテムの取得を防ぐ
        return !restricted && target.mayPickup(player);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // 制限されている場合、アイテムの配置を防ぐ
        return !restricted && target.mayPlace(stack);
    }

    // === 元のスロットへの委譲 ===

    @Override
    public void onTake(Player player, ItemStack stack) {
        target.onTake(player, stack);
    }

    @Override
    public ItemStack getItem() {
        return target.getItem();
    }

    @Override
    public boolean hasItem() {
        return target.hasItem();
    }

    @Override
    public void setByPlayer(ItemStack stack, ItemStack oldStack) {
        target.setByPlayer(stack, oldStack);
    }

    @Override
    public void set(ItemStack stack) {
        target.set(stack);
    }

    @Override
    public void setChanged() {
        target.setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return target.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return target.getMaxStackSize(stack);
    }

    @Nullable
    @Override
    public ResourceLocation getNoItemIcon() {
        return target.getNoItemIcon();
    }

    @Override
    public ItemStack remove(int amount) {
        return target.remove(amount);
    }

    @Override
    public int getSlotIndex() {
        return target.getSlotIndex();
    }

    @Override
    public boolean isSameInventory(Slot other) {
        return target.isSameInventory(other);
    }

    @Override
    public Slot setBackground(ResourceLocation sprite) {
        target.setBackground(sprite);
        return this;
    }

    /**
     * 元のスロットを取得
     */
    public Slot getTarget() {
        return target;
    }
}
