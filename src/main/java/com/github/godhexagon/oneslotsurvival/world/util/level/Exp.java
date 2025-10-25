package com.github.godhexagon.oneslotsurvival.world.util.level;

import com.github.godhexagon.oneslotsurvival.world.util.attribute.MainRoleExp;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * メインロールのレベルアップに必要な残り経験値を管理するユーティリティクラス
 *
 * <p>このクラスは、プレイヤーが次のレベルに到達するまでに必要な経験値を追跡します。
 * 経験値を獲得すると残り経験値が減少し、0以下になるとレベルアップが可能になります。</p>
 */
public class Exp {
    /**
     * 指定されたプレイヤーのメインロールレベルアップに必要な残り経験値を取得
     *
     * @param player チェックするプレイヤー
     * @return 次のレベルに到達するまでに必要な残り経験値
     */
    public static double getRemainingMain(Player player) {
        return MainRoleExp.getExp(player);
    }

    /**
     * 指定されたプレイヤーのメインロールレベルアップに必要な残り経験値を設定
     *
     * @param player 変更するプレイヤー
     * @param remaining 設定する残り経験値（0以上）
     * @throws IllegalArgumentException 残り経験値が0未満の場合
     */
    public static void setRemainingMain(ServerPlayer player, double remaining) {
        MainRoleExp.setExp(player, remaining);
    }

    /**
     * 指定されたプレイヤーのメインロール経験値を進行させる（残り経験値を減少させる）
     *
     * <p>プレイヤーが経験値を獲得すると、次のレベルに到達するまでに必要な残り経験値が減少します。
     * 残り経験値が0以下になった場合は、レベルアップ処理を別途実行する必要があります。</p>
     *
     * @param player 変更するプレイヤー
     * @param amount 獲得した経験値（減算する量）
     */
    public static void incrementMain(ServerPlayer player, double amount) {
        double remaining = getRemainingMain(player);
        MainRoleExp.setExp(player, remaining - amount);
    }
}
