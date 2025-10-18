package com.github.godhexagon.oneslotsurvival.world.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import com.github.godhexagon.oneslotsurvival.world.util.SlotBarrierFilling;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Forge API用サーバー側（Dedicated/Integrated）イベントハンドラー
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID)
public class ServerEvent {
    /**
     * ここでは、プレイヤーに配布されるスロットバリアを適切な状態に保つために、ゲームモードが変更されたときのイベントをハンドリング
     *
     * @param event *Forge API
     */
    @SubscribeEvent
    private static void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        // パターンマッチングでServerPlayerに自動キャスト（Java 16+の流儀）
        // サーバーのときだけしかServerPlayerでない仕様を利用
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        boolean wasEffective = PlayerModValidity.isEffective(serverPlayer, event.getCurrentGameMode());
        boolean willBeEffective = PlayerModValidity.isEffective(serverPlayer, event.getNewGameMode());

        // 状態変化がない場合は何もしない
        if (wasEffective == willBeEffective) {
            return;
        }

        if (willBeEffective) {
            SlotBarrierFilling.fillUp(serverPlayer);
        } else {
            SlotBarrierFilling.clean(serverPlayer);
        }
    }
}
