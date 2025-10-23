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
 * Forge API用クライアント側イベントハンドラ。
 * ここでは、ホットバー制御とカスタムインベントリ挿入とインベントリースロットツールバー改変をしている。
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID, value = Dist.CLIENT)
public class ClientEvent {

    /**
     * 各クライアントでティック処理。
     * ここでは、ホットバーの制御を改変している。
     *
     * @param event *Forge API
     */
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }

        if (PlayerModValidity.isEffective(mc.player)) {
            enforceMainHandSelection(mc.player);
        }
    }

    private static final int MAX_SLOT_INDEX = 8;

    /**
     * クライアントティック中にホットバー選択を正しい範囲に制限
     */
    public static void enforceMainHandSelection(Player player) {
        enforceMainHandSelection(player, 4);
    }

    public static void enforceMainHandSelection(Player player, int exclusiveEnd) {
        // Use reflection to access private selected field if available
        try {
            var inventory = player.getInventory();
            var selectedField = inventory.getClass().getDeclaredField("selected");
            selectedField.setAccessible(true);
            int currentSelected = selectedField.getInt(inventory);

            int threshold = Math.floorDiv(MAX_SLOT_INDEX + exclusiveEnd, 2);
            if (threshold < currentSelected) {
                selectedField.setInt(inventory, exclusiveEnd - MAX_SLOT_INDEX -1 + currentSelected);
            } else if (exclusiveEnd <= currentSelected) {
                selectedField.setInt(inventory, currentSelected - exclusiveEnd);
            }
        } catch (Exception e) {
            // Reflection failed, silently ignore
        }
    }

    /**
     * クライアントで画面が開く直前に呼び出される
     * プレイヤーのインベントリ画面の場合、RestrictedInventoryScreen に置き換えて
     * インベントリとホットバースロットを制限
     *
     * @param event *Forge API
     */
    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // 開こうとしている画面がプレイヤーのインベントリかチェック
        // RestrictedInventoryScreenはInventoryScreenを継承しているため除外（これがないとスロットラッパーが多重ラッピングになる問題が確認されている）
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
     * @param event *Forge API
     */
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        @Nullable Player player = event.getEntity();

        if (player != null && PlayerModValidity.isEffective(player) && event.getItemStack().is(ModItems.SLOT_BARRIER.get())) {
            event.getToolTip().clear();
        }
    }
}