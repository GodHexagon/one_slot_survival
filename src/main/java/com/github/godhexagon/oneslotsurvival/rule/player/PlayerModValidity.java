package com.github.godhexagon.oneslotsurvival.rule.player;

import com.github.godhexagon.oneslotsurvival.rule.attribute.ModEnabled;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

/**
 * どのプレイヤーが One Slot モードを有効にしているかを追跡する処理の集合。
 * 自動永続化とクライアント同期のために Attribute システムを使用
 * Attributeシステムをラップする。
 */
public class PlayerModValidity {
    /**
     * 指定されたプレイヤーに対して One Slot モード有効性のワールド設定をチェック
     * クライアントとサーバーの両側で動作 - 自動的に同期される
     *
     * @param player チェックするプレイヤー
     * @return このプレイヤーに対して One Slot モードが有効な場合は true
     * @throws IllegalStateException MOD_ENABLED Attribute が登録されていない場合
     */
    public static boolean isEnabled(Player player) {
        return ModEnabled.getEnabled(player);
    }

    /**
     * 指定されたプレイヤーに対して One Slot モードが実際に有効か、ゲームモードと照らし合わせて適切かチェック
     * クライアントとサーバーの両側で動作 - 自動的に同期される
     *
     * @param player チェックするプレイヤー
     * @return このプレイヤーに対して One Slot モードが有効な場合は true
     * @throws IllegalStateException MOD_ENABLED Attribute が登録されていない場合
     */
    public static boolean isEffective(Player player) {
        return isEffective(player, player.gameMode());
    }

    public static boolean isEffective(Player player, GameType mode) {
        return (mode == GameType.ADVENTURE || mode == GameType.SURVIVAL) && isEnabled(player);
    }

    /**
     * 指定されたプレイヤーに対して One Slot モードを有効または無効にする
     * 有効化時は、既存のインベントリアイテムを処理
     * 自動的に永続化され、クライアントと同期される
     *
     * @param player 変更するプレイヤー
     * @param enabled One Slot モードを有効にする場合は true、無効にする場合は false
     * @throws IllegalStateException MOD_ENABLED Attribute が登録されていないか、プレイヤーに存在しない場合
     */
    public static void setEnabled(ServerPlayer player, boolean enabled) {
        // 設定を永続化
        ModEnabled.updateEnabled(player, enabled);
    }

    /**
     * 指定されたプレイヤーの One Slot モードをトグル
     * 自動的に永続化され、クライアントと同期される
     *
     * @param player トグルするプレイヤー
     * @return 新しい状態（有効になった場合は true、無効になった場合は false）
     * @throws IllegalStateException MOD_ENABLED Attribute が登録されていないか、プレイヤーに存在しない場合
     */
    public static boolean toggle(ServerPlayer player) {
        boolean newState = !isEnabled(player);
        setEnabled(player, newState);
        return newState;
    }
}