package com.github.godhexagon.oneslotsurvival.rule.role;

import com.github.godhexagon.oneslotsurvival.rule.attribute.MainRoleId;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * プレイヤーのロール管理ユーティリティ
 * <p>
 * MainRoleIdの低レベルAPIをラップし、MainRole enumを使った
 * 高レベルなロール操作を提供する。
 * </p>
 */
public class RoleManager {

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
    public static void setRole(ServerPlayer player, MainRole role) {
        MainRoleId.setRoleId(player, role.getAtttributeId());
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
    public static MainRole getRole(Player player) {
        int roleId = MainRoleId.getRoleId(player);
        return MainRole.fromAttributeId(roleId);
    }

    /**
     * プレイヤーのロールをクリア（未割り当て状態にする）
     *
     * @param player 変更するプレイヤー
     * @throws IllegalStateException MAIN_ROLE_ID Attributeが登録されていないか、プレイヤーに存在しない場合
     */
    public static void clearRole(ServerPlayer player) {
        setRole(player, MainRole.UNASSIGNED);
    }

    /**
     * プレイヤーにロールが割り当てられているかチェック
     *
     * @param player チェックするプレイヤー
     * @return ロールが割り当てられている場合true
     */
    public static boolean hasRole(Player player) {
        MainRole role = getRole(player);
        return role != MainRole.ERROR && role != MainRole.UNASSIGNED;
    }
}
