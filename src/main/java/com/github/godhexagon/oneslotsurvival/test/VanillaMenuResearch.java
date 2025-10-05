package com.github.godhexagon.oneslotsurvival.test;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * ダミー継承クラス - バニラのスロット操作メソッド（サーバーサイド）を調査するためのテストクラス
 * AbstractContainerMenu はサーバーサイドでのアイテム移動処理を担当する
 */
public class VanillaMenuResearch extends AbstractContainerMenu {

    protected VanillaMenuResearch(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        // Shift+クリックでのアイテム移動処理
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    // IDE の自動補完で doClick などのメソッドを探す
    // doClick メソッドがアイテム移動の実装を持っているはず

    // doClick メソッドをオーバーライドして署名を確認
    // doClick は private なのでオーバーライドできない
    // 代わりに clicked メソッドをオーバーライドする
    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        // clicked: スロットクリックのサーバーサイド処理
        // この中で doClick が呼ばれているはず

        // IDE で Ctrl+クリック または F12 で super.clicked の実装を確認できる
        // AbstractContainerMenu のソースコードが表示されるはず
        super.clicked(slotId, button, clickType, player);

        // 確認すべきポイント:
        // 1. clicked() から doClick() がどのように呼ばれるか
        // 2. doClick() 内での ClickType 別の処理分岐
        // 3. PICKUP, SWAP などの各ケースでのアイテム移動処理
    }
}
