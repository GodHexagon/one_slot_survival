package com.github.godhexagon.oneslotsurvival.client.mixin;

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
 * One Slot Survival mod において、スロットバリアへのクリックを無効化する Mixin
 * AbstractContainerScreen の slotClicked メソッドをターゲットにして、
 * プレイヤーが制限されている場合に禁止されたスロットとの相互作用を防ぐ
 */
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    /**
     * スロットバリアへのクリックを防ぐため slotClicked メソッドに注入
     * この注入は、バニラ処理が行われる前のメソッドの先頭（HEAD）で発生する
     *
     * @param slot クリックされたスロット
     * @param slotId スロットのID
     * @param mouseButton 使用されたマウスボタン（0=左、1=右、2=中央）
     * @param type クリックのタイプ（PICKUP、QUICK_MOVE、SWAP など）
     * @param ci 注入を制御するための CallbackInfo
     */
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClicked(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        // null スロット（インベントリ外のクリック）を許可
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
            // コールバックをキャンセルしてスロットクリックを中止
            ci.cancel();
        }
    }
}