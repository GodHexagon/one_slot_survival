package com.github.godhexagon.oneslotsurvival.rule.role;

import com.github.godhexagon.oneslotsurvival.rule.attribute.MainRoleId;
import com.github.godhexagon.oneslotsurvival.rule.attribute.SubRoleId;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * プレイヤーのロール管理ユーティリティ
 * <p>
 * MainRoleIdとSubRoleIdの低レベルAPIをラップし、
 * MainRole/SubRole enumを使った高レベルなロール操作を提供する。
 * </p>
 */
public class RoleManager {

    // ===== メインロール関連 =====

    /**
     * プレイヤーにメインロールを設定
     * <p>
     * 自動的に永続化され、クライアントと同期される。
     * </p>
     *
     * @param player 変更するプレイヤー
     * @param role 設定するロール
     * @throws IllegalStateException MAIN_ROLE_ID Attributeが登録されていないか、プレイヤーに存在しない場合
     */
    public static void setMainRole(ServerPlayer player, MainRole role) {
        MainRoleId.setRoleId(player, role.getAttributeId());
    }

    /**
     * プレイヤーのメインロールを取得
     * <p>
     * クライアントとサーバーの両側で動作 - 自動的に同期される。
     * </p>
     *
     * @param player チェックするプレイヤー
     * @return プレイヤーのメインロール
     */
    public static MainRole getMainRole(Player player) {
        int roleId = MainRoleId.getRoleId(player);
        return MainRole.fromAttributeId(roleId);
    }

    /**
     * プレイヤーのメインロールを取得（後方互換性のため）
     * <p>
     * @deprecated 代わりに getMainRole() を使用してください
     * </p>
     *
     * @param player チェックするプレイヤー
     * @return プレイヤーのメインロール
     */
    @Deprecated
    public static MainRole getRole(Player player) {
        return getMainRole(player);
    }

    // ===== サブロール関連 =====

    /**
     * プレイヤーにサブロールを設定
     * <p>
     * 自動的に永続化され、クライアントと同期される。
     * </p>
     *
     * @param player 変更するプレイヤー
     * @param role 設定するサブロール
     * @throws IllegalStateException SUB_ROLE_ID Attributeが登録されていないか、プレイヤーに存在しない場合
     */
    public static void setSubRole(ServerPlayer player, SubRole role) {
        SubRoleId.setRoleId(player, role.getAttributeId());
    }

    /**
     * プレイヤーのサブロールを取得
     * <p>
     * クライアントとサーバーの両側で動作 - 自動的に同期される。
     * </p>
     *
     * @param player チェックするプレイヤー
     * @return プレイヤーのサブロール
     */
    public static SubRole getSubRole(Player player) {
        int roleId = SubRoleId.getRoleId(player);
        return SubRole.fromAttributeId(roleId);
    }

    // ===== 共通機能 =====

    /**
     * プレイヤーのロールをクリア（メインとサブの両方を未割り当て状態にする）
     *
     * @param player 変更するプレイヤー
     * @throws IllegalStateException ROLE_ID Attributeが登録されていないか、プレイヤーに存在しない場合
     */
    public static void clearRole(ServerPlayer player) {
        setMainRole(player, MainRole.UNASSIGNED);
        setSubRole(player, SubRole.UNASSIGNED);
    }

    /**
     * プレイヤーのメインロールをクリア（未割り当て状態にする）
     *
     * @param player 変更するプレイヤー
     * @throws IllegalStateException MAIN_ROLE_ID Attributeが登録されていないか、プレイヤーに存在しない場合
     */
    public static void clearMainRole(ServerPlayer player) {
        setMainRole(player, MainRole.UNASSIGNED);
    }

    /**
     * プレイヤーのサブロールをクリア（未割り当て状態にする）
     *
     * @param player 変更するプレイヤー
     * @throws IllegalStateException SUB_ROLE_ID Attributeが登録されていないか、プレイヤーに存在しない場合
     */
    public static void clearSubRole(ServerPlayer player) {
        setSubRole(player, SubRole.UNASSIGNED);
    }

    /**
     * プレイヤーは進捗が進行するのに適格かチェック
     *
     * @param player チェックするプレイヤー
     * @return 適格であるときtrue
     */
    public static boolean isPossibleRoleProgress(Player player) {
        MainRole mainRole = getMainRole(player);
        SubRole subRole = getSubRole(player);
        // どちらか片方でも有効ならレベルアップするべき
        return (mainRole != MainRole.ERROR && mainRole != MainRole.UNASSIGNED) || (subRole != SubRole.ERROR && subRole != SubRole.UNASSIGNED);
    }
}
