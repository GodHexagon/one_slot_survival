package com.github.godhexagon.oneslotsurvival.mixin.client;

import com.github.godhexagon.oneslotsurvival.world.util.inventory.SlotRestriction;
import com.github.godhexagon.oneslotsurvival.world.util.player.PlayerModValidity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * ScrollWheelHandlerにロールスロット制限を適用するMixin。
 * ホットバーのマウスホイール選択を解放されたスロット範囲内に制限します。
 */
@Mixin(ScrollWheelHandler.class)
public class ScrollWheelHandlerMixin {

    /**
     * マウスホイールによるホットバー選択のループ処理を
     * 解放されたスロット範囲内に制限します。
     */
    @Inject(method = "getNextScrollWheelSelection", at = @At("HEAD"), cancellable = true)
    private static void onGetNextScrollWheelSelection(double scrollDelta, int currentSlot, int selectionSize, CallbackInfoReturnable<Integer> cir) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && PlayerModValidity.isEffective(player)) {
            // 解放されたスロット数をカウント
            int roleSlotCount = 0;
            for (int i = 1; i <= 3; i++) {
                if (SlotRestriction.unlockedRoleSlot(i, player)) {
                    roleSlotCount++;
                }
            }
            // 解放されたスロット数（例：ロールスロット3つ → インデックス0-3が許可 → maxSlot=4）
            int maxSlot = roleSlotCount + 1;

            // バニラのロジックを解放範囲内で再実装
            int direction = (int)Math.signum(scrollDelta);
            int newSlot = currentSlot - direction;
            newSlot = Math.max(-1, newSlot);

            // 解放範囲内でループ
            while (newSlot < 0) {
                newSlot += maxSlot;
            }

            while (newSlot >= maxSlot) {
                newSlot -= maxSlot;
            }

            cir.setReturnValue(newSlot);
        }
    }
}
