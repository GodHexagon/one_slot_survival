package com.github.godhexagon.oneslotsurvival.client.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.client.gui.RestrictedInventoryScreen;
import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/**
 * インベントリ関連イベント用のクライアント側イベントハンドラ
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, value = Dist.CLIENT)
public class ClientEvent {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        enforceMainHandSelection();
    }

    /**
     * クライアントティック中にホットバー選択をスロット 0（メインハンド）に固定
     */
    private static void enforceMainHandSelection() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        // ローカルプレイヤーが制限されている場合のみ選択を強制
        if (PlayerModValidity.isEffective(mc.player)) {
            // リフレクションを使用してプライベート selected フィールドにアクセス
            try {
                var inventory = mc.player.getInventory();
                var selectedField = inventory.getClass().getDeclaredField("selected");
                selectedField.setAccessible(true);
                int currentSelected = selectedField.getInt(inventory);

                if (currentSelected != 0) {
                    selectedField.setInt(inventory, 0);
                }
            } catch (Exception e) {
                // リフレクション失敗時は黙って無視
            }
        }
    }

    /**
     * クライアントで画面が開く直前に呼び出される
     * プレイヤーのインベントリ画面の場合、RestrictedInventoryScreen に置き換えて
     * インベントリとホットバースロットを制限
     *
     * @param event 画面オープニングイベント
     */
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // 開こうとしている画面がプレイヤーのインベントリかチェック
        // ただし、すでに制限画面の場合は無限ループを避けるため除外
        if (event.getNewScreen() instanceof InventoryScreen &&
            !(event.getNewScreen() instanceof RestrictedInventoryScreen)) {

            Minecraft minecraft = Minecraft.getInstance();

            // 有効なクライアントプレイヤーと接続があることを確認
            if (minecraft.player != null && PlayerModValidity.isEffective(minecraft.player)) {
                // バニラの InventoryScreen を RestrictedInventoryScreen に置き換え
                event.setNewScreen(new RestrictedInventoryScreen(minecraft.player));
            }
        }
    }

    /**
     * バリアアイテムのツールチップを無効化する。
     *
     * @param event
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        @Nullable Player player = event.getEntity();

        if (player != null && PlayerModValidity.isEffective(player) && event.getItemStack().is(ModItems.SLOT_BARRIER.get())) {
            event.getToolTip().clear();
        }
    }
}