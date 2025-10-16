package com.github.godhexagon.oneslotsurvival.mixin;

import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to disable slot clicking on restricted slots for One Slot Survival mod.
 * Targets the slotClicked method in AbstractContainerScreen to prevent interaction
 * with prohibited slots when the player is restricted.
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    /**
     * Inject into the slotClicked method to prevent clicks on restricted slots.
     * This injection happens at the HEAD (beginning) of the method before any
     * vanilla processing occurs.
     *
     * @param slot The slot being clicked
     * @param slotId The ID of the slot
     * @param mouseButton The mouse button used (0=left, 1=right, 2=middle)
     * @param type The type of click (PICKUP, QUICK_MOVE, SWAP, etc.)
     * @param ci CallbackInfo for controlling the injection
     */
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClicked(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        // Allow null slots (clicking outside inventory)
        if (slot == null) {
            return;
        }

        // クライアントのプレイヤーインスタンスを取得
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }

        // 対象プレイヤー以外は除外
        if (!PlayerModValidity.isEffective(mc.player)) {
            return;
        }

        // バリアアイテムは触れない
        if (slot.getItem().is(ModItems.SLOT_BARRIER.get())) {
            // Cancel the slot click by cancelling the callback
            ci.cancel();
        }
    }
}