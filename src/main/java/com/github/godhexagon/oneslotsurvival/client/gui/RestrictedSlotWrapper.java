package com.github.godhexagon.oneslotsurvival.client.gui;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

/**
 * スロットラッパー
 *
 * <p>このクラスは {@link net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.SlotWrapper}
 * の設計パターンをコピーして実装されています。</p>
 *
 * <h2>CreativeModeInventoryScreen.SlotWrapper との類似点</h2>
 * <ul>
 *   <li>元のスロットをラップして表示位置を変更</li>
 *   <li>すべてのメソッドを元の target スロットに委譲</li>
 *   <li>必要に応じてメソッドをオーバーライドして動作をカスタマイズ</li>
 * </ul>
 *
 * <h2>現在の実装機能</h2>
 * <ul>
 *   <li>{@code isActive()}, {@code mayPickup()}, {@code mayPlace()} を元スロットへ完全に移譲</li>
 *   <li>表示位置のみを変更（画面外 -2000, -2000 など）</li>
 *   <li>restricted フラグは残されているが、現在は使用されていない（将来の拡張用）</li>
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

    /**
     * @param target 元のスロット
     * @param index スロットインデックス（menu.slots内の位置）
     * @param x 表示X座標（-2000で画面外）
     * @param y 表示Y座標（-2000で画面外）
     */
    public RestrictedSlotWrapper(Slot target, int index, int x, int y) {
        super(target.container, target.getContainerSlot(), x, y);
        this.target = target;
        // 重要: スロットのindexを正しく設定する
        this.index = index;
    }

    // === 元のスロットへの委譲 ===

    @Override
    public boolean isActive() {
        // 元スロットの isActive() をそのまま返す
        return target.isActive();
    }

    @Override
    public boolean mayPickup(Player player) {
        // 元スロットの mayPickup() をそのまま返す
        return target.mayPickup(player);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // 元スロットの mayPlace() をそのまま返す
        return target.mayPlace(stack);
    }

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
