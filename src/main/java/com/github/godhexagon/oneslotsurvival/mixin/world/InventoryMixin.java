package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inventoryクラスに仮想スロット機能を追加するMixin
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow
    @Final
    private NonNullList<ItemStack> items;
    @Shadow
    @Final
    public Player player;

    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void onGetFreeSlot(CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player))
        {
            cir.cancel();

            for (int i = 0; i < this.items.size(); i++) {
                if (!one_slot_survival$isRestrictedSlot(i) && this.items.get(i).isEmpty()) {
                    cir.setReturnValue(i);
                    return;
                }
            }

            cir.setReturnValue(-1);
        }
    }

    /**
     * 制限対象のスロットかどうかを判定
     *
     * @param index スロットインデックス
     * @return true の場合、制限対象
     */
    @Unique
    private boolean one_slot_survival$isRestrictedSlot(int index) {
        // メインハンドスロット（index=0）のみ許可
        return 1 <= index && index <= 35;
    }
}
