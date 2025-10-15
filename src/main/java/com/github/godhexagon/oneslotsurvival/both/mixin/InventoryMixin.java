package com.github.godhexagon.oneslotsurvival.both.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inventoryクラスに仮想スロット機能を追加するMixin
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {

    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void onGetFreeSlot(CallbackInfoReturnable<Integer> cir) {
        cir.cancel();

        for (int i = 0; i < this.items.size(); i++) {
            if (isRestrictedSlot(i)) {
                continue;
            }

            if (this.items.get(i).isEmpty()) {
                cir.setReturnValue(i);
            }
        }

        cir.setReturnValue(-1);
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
        if (index >= 40 && index <= 44) {
            return true;
        }

        // その他のスロット（クラフト、防具、オフハンド、メインハンド）は制限しない
        return false;
    }
}
