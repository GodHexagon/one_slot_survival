package com.github.godhexagon.oneslotsurvival.server.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.server.service.SlotBarrierFilling;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID)
public class ServerEvent {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;

        // Only process on server side
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
