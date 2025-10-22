package com.github.godhexagon.oneslotsurvival.world.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.world.util.RoleSlot;
import com.github.godhexagon.oneslotsurvival.world.util.SlotBarrierFilling;
import com.github.godhexagon.oneslotsurvival.world.util.InventoryDefinition;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingSwapItemsEvent;
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
        }
    }

    /**
     * オフハンドとメインハンドのアイテム入れ替え（Fキー）を制限する。
     * つるはしはロールスロット専用アイテムなので、オフハンドからの直接移動を禁止する。
     *
     * @param event Forge API
     * @return trueの場合キャンセル（入れ替えを防止）
     */
    @SubscribeEvent
    public static boolean onLivingSwapHandItems(LivingSwapItemsEvent.Hands event) {
        // プレイヤーのみ対象
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return false;
        }

        // MOD有効なプレイヤーのみ対象
        if (!PlayerModValidity.isEffective(player)) {
            return false;
        }

        // ロールスロットに対する入れ替えだけ制御
        if (!InventoryDefinition.isRoleSlot(player.getInventory().getSelectedSlot())) {
            return false;
        }

        // オフハンド → メインハンドへ移動される予定のアイテム
        ItemStack offhandItem = event.getItemSwappedToMainHand();

        // つるはし以外のアイテムは移動をキャンセル
        if (!offhandItem.isEmpty() && !RoleSlot.isPickaxe(offhandItem)) {
            return true; // キャンセル
        }

        return false; // 続行
    }
}
