package com.github.godhexagon.oneslotsurvival.world.util.level;

import com.github.godhexagon.oneslotsurvival.world.util.attribute.MainRoleLevel;
import com.github.godhexagon.oneslotsurvival.world.util.attribute.MainRoleRemainingExp;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * メインロールの経験値を管理するユーティリティクラス
 *
 * <p>このクラスは、プレイヤーのメインロール経験値を操作するための静的メソッドを提供します。
 * 経験値は「残り経験値」として管理されており、経験値を獲得すると残り経験値が減少します。</p>
 *
 * <h2>経験値システムの仕様</h2>
 * <ul>
 *   <li>経験値獲得 = 残り経験値を減算</li>
 *   <li>残り経験値が0以下になると、レベルアップが発生する（別システムで管理）</li>
 *   <li>経験値の単位は実数（double）で管理</li>
 * </ul>
 */
public class Exp {
    private static final double LEVEL_EXP = 2000;

    /**
     * メインロールの経験値を加算します（残り経験値を減算します）
     *
     * <p>このメソッドは、プレイヤーが何らかのアクションを行った際に経験値を獲得する際に使用されます。
     * 経験値は「残り経験値」として管理されているため、経験値を獲得すると残り経験値が減少します。</p>
     *
     * <p><b>使用例:</b></p>
     * <pre>{@code
     * // プレイヤーが100ブロック歩いたときに5.0経験値を付与
     * Exp.addMain(player, 5.0);
     *
     * // ダメージを与えたときに、ダメージ量に応じた経験値を付与
     * Exp.addMain(player, damageAmount * 0.1);
     * }</pre>
     *
     * @param player 経験値を付与するプレイヤー
     * @param amount 付与する経験値量（正の数）
     */
    public static void addMain(ServerPlayer player, double amount) {
        // まだレベルシステムに参加していないプレイヤーは、最初の経験値を設定
        if (MainRoleLevel.getLevel(player) <= 0) {
            MainRoleLevel.setLevel(player, 1);
            MainRoleRemainingExp.setRemainingExp(player, LEVEL_EXP - amount);
            return;
        }

        double remaining = MainRoleRemainingExp.getRemainingExp(player);

        // レベルアップ処理
        if (remaining < 0) {
            MainRoleLevel.setLevel(player, MainRoleLevel.getLevel(player) + 1);
            remaining += LEVEL_EXP;
        }

        MainRoleRemainingExp.setRemainingExp(player, remaining - amount);
    }

    /**
     * プレイヤーのメインロールのレベルを取得する。
     * 
     * @param player
     * @return レベル。
     */
    public static int getMainLevel(Player player) {
        return Math.max(1, MainRoleLevel.getLevel(player));
    }
}
