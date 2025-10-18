package com.github.godhexagon.oneslotsurvival.server.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.server.service.SlotBarrierFilling;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge API用サーバー（Dedicated/Integrated）イベントハンドラー
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
        Player player = event.player;

        // サーバー側でのみ処理
        if (player.level().isClientSide) {
            return;
        }

        // スロットバリアで埋める必要があるプレイヤーだけ
        if (PlayerModValidity.isEffective(player) && SlotBarrierFilling.shouldBeFilledUp(player)) {
            // スロットバリアで埋める
            SlotBarrierFilling.fillUp(player);
        } else if (!PlayerModValidity.isEffective(player) && SlotBarrierFilling.shouldBeClean(player)) {
            SlotBarrierFilling.clean(player);
        }
    }
}
