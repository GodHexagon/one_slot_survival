package com.github.godhexagon.oneslotsurvival.world.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.world.util.SlotBarrierFilling;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge API用サーバー側（Dedicated/Integrated）イベントハンドラー
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID)
public class ServerEvent {
    /**
     * ここでは、プレイヤーに配布されるスロットバリアを常に適切な状態に保つ処理を行っている。
     *
     * @param event *Forge API
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // サーバーのときだけしかServerPlayerでない仕様を利用
        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // スロットバリアで埋める必要があるプレイヤーだけ
        if (PlayerModValidity.isEffective(serverPlayer) && SlotBarrierFilling.shouldBeFilledUp(serverPlayer)) {
            // スロットバリアで埋める
            SlotBarrierFilling.fillUp(serverPlayer);
        } else if (!PlayerModValidity.isEffective(serverPlayer) && SlotBarrierFilling.shouldBeClean(serverPlayer)) {
            SlotBarrierFilling.clean(serverPlayer);
        }
    }
}
