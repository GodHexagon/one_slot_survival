package com.github.godhexagon.oneslotsurvival.world.event;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.github.godhexagon.oneslotsurvival.rule.attribute.Initialized;
import com.github.godhexagon.oneslotsurvival.rule.inventory.SlotBarrierFilling;
import com.github.godhexagon.oneslotsurvival.rule.inventory.SlotRestriction;
import com.github.godhexagon.oneslotsurvival.rule.player.PlayerModValidity;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleDistribution;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;
import com.github.godhexagon.oneslotsurvival.world.storage.BonusItem;
import com.github.godhexagon.oneslotsurvival.world.storage.DefaultRole;
import com.github.godhexagon.oneslotsurvival.world.storage.WorldOptions;
import com.mojang.logging.LogUtils;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingSwapItemsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Forge API用サーバー側（Dedicated/Integrated）イベントハンドラー
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID)
public class WorldEvent {
    private static final Logger LOGGER = LogUtils.getLogger();
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

        // オフハンド → メインハンドへ移動される予定のアイテム
        ItemStack offhandItem = event.getItemSwappedToMainHand();
        // Inventoryクラスのインデックス
        int inventoryIndex = player.getInventory().getSelectedSlot();

        // ロールスロットルールにおいても許可されるなら大丈夫
        if (SlotRestriction.isEligibleItemForRoledPlayer(offhandItem, inventoryIndex, player)) {
            return false;
        }

        return true; // キャンセル
    }

    /**
     * プレイヤーのログイン時に、ワールド新規参加者にデフォルトロールを設定する。
     * <p>
     * 判定基準: バニラ統計データ PLAY_TIME が 0 の場合、このワールドに初めて参加したと判定
     * </p>
     * <p>
     * この方式により以下の要件を満たす:
     * <ul>
     *   <li>ワールドに新規で参加するとき: デフォルト設定を適用 ○</li>
     *   <li>ディメンション移動: このイベントは発火しない ✖</li>
     *   <li>既存ワールドに新しくMODを導入したとき: PLAY_TIME > 0 なので適用されない ✖</li>
     * </ul>
     * </p>
     *
     * @param event Forge API
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // サーバー側のみ処理
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // このワールドに初めて参加したときの処理
        boolean initialized = Initialized.getInitialized(player);
        if (!initialized) {
            // 処理をしたことがあるフラグを追加
            Initialized.updateInitialized(player, true);

            MinecraftServer server = player.getServer();

            // MOD有効性を設定
            boolean defaultModValidity = WorldOptions.isDefaultModValidityEnabled(server);
            PlayerModValidity.setEnabled(player, defaultModValidity);

            // デフォルトロールを設定
            DefaultRole defaultRole = WorldOptions.getDefaultRole(server);
            if (defaultRole == DefaultRole.UNASSIGN) {
                RoleManager.setMainRole(player, MainRole.UNASSIGNED);
                RoleManager.setSubRole(player, SubRole.UNASSIGNED);
            } else if (defaultRole == DefaultRole.RANDOM) {
                RoleDistribution.setRandom(player);
            } else if (defaultRole == DefaultRole.DEFINED_LIST) {
                // リスト利用状況が永続化される機能を利用
                RoleDistribution.setDefinedListWithCounting(player);
            }

            // ボーナスアイテムを配布
            BonusItem bonusItemMode = WorldOptions.getBonusItem(server);
            // バンドルを配布
            if (bonusItemMode == BonusItem.BUNDLE || bonusItemMode == BonusItem.BUNDLE_RESPAWN) {
                player.addItem(new ItemStack(Items.BUNDLE));
            } else if (bonusItemMode == BonusItem.SHULKERBOX || bonusItemMode == BonusItem.SHULKERBOX_RESPAWN) {
                player.addItem(new ItemStack(Items.SHULKER_BOX));
            }

            // ログ出力
            LOGGER.info("The first login process has been applied to {}", player.getName().getString());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        // サーバー側のみ処理
        // サーバーのときだけしかServerPlayerでない仕様を利用
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        // MOD有効なプレイヤーのみ対象
        if (!PlayerModValidity.isEffective(player)) {
            return;
        }

        // ボーナスアイテムを配布
        BonusItem bonusItemMode = WorldOptions.getBonusItem(player.getServer());
        if (bonusItemMode == BonusItem.BUNDLE_RESPAWN) {
            // バンドルを配布
            player.addItem(new ItemStack(Items.BUNDLE));
        } else if (bonusItemMode == BonusItem.SHULKERBOX_RESPAWN) {
            // シュルカーボックスを配布
            player.addItem(new ItemStack(Items.SHULKER_BOX));
        }
    }
}
