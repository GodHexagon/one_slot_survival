package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import com.github.godhexagon.oneslotsurvival.world.util.InventoryDefinition;
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

    /**
     * 指定されたスロットインデックスがロールスロット無視対象かどうかを判定する
     *
     * @param slotIndex チェックするスロットインデックス
     * @return true: スロットをスキップすべき, false: スロットを処理すべき
     */
    @Unique
    private boolean one_slot_survival$shouldSkipSlot(int slotIndex) {
        // ロールスロットでなおかつ空の場合は無視される
        if (InventoryDefinition.isRoleSlot(slotIndex) && this.items.get(slotIndex).isEmpty()) {
            return true;
        }
        // 正常な制限スロットは無視される
        if (InventoryDefinition.isDisableSlot(slotIndex) && this.items.get(slotIndex).is(ModItems.SLOT_BARRIER.get())) {
            return true;
        }
        return false;
    }

    /**
     * 空のスロットを探索するメソッドへの挿入。
     * ここでは、対象プレイヤーについて、ロールスロットへの拾得を制限する改変をしている。
     *
     * @param cir *Forge API
     */
    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void onGetFreeSlot(CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player)) {
            cir.cancel();

            for (int i = 0; i < this.items.size(); i++) {
                if (one_slot_survival$shouldSkipSlot(i)) {
                    continue;
                }

                // バニラの処理
                if (this.items.get(i).isEmpty()) {
                    cir.setReturnValue(i);
                    return;
                }
            }

            cir.setReturnValue(-1);
        }
    }
}
