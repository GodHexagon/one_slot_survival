package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import com.github.godhexagon.oneslotsurvival.world.util.SlotDefinition;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inventoryクラスに仮想スロット機能を追加するMixin
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("OneSlotSurvival/InventoryMixin");

    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    @Shadow
    @Final
    public Player player;

    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void onGetFreeSlot(CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(player)) {
            for (int i = 0; i < this.items.size(); i++) {
                if (!SlotDefinition.isRestrictedSlot(i) && this.items.get(i).isEmpty()) {
                    cir.setReturnValue(i);
                    return;
                }
            }

            cir.setReturnValue(-1);
        }
    }
}
