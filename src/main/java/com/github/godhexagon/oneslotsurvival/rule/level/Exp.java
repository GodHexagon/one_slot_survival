package com.github.godhexagon.oneslotsurvival.rule.level;

import com.github.godhexagon.oneslotsurvival.object.config.ExpConfig;
import com.github.godhexagon.oneslotsurvival.rule.attribute.LevelAttribute;
import com.github.godhexagon.oneslotsurvival.rule.attribute.RemainingExpAttribute;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 永続化レイヤーを隠し、経験値のルールを提供する。
 *
 * <p>このクラスは、プレイヤーのメインロール経験値を操作するための静的メソッドを提供します。
 * 経験値は「残り経験値」として管理されており、経験値を獲得すると残り経験値が減少します。</p>
 *
 * <h2>経験値システムの仕様</h2>
 * <ul>
 *   <li>経験値獲得 = 残り経験値を減算</li>
 *   <li>残り経験値が0以下になると、レベルアップが発生する</li>
 *   <li>経験値の単位は実数（double）で管理</li>
 * </ul>
 */
public class Exp {
    /**
     * プレイヤーの経験値の蓄積を返す。
     * 
     * @param player プレイヤー。
     * @return 次のレベルアップまでの残り経験値。
     */
    public static double get(Player player) {
        // まだレベルシステムに参加していないプレイヤーは、最初の経験値を設定
        if (LevelAttribute.getLevel(player) <= Level.UNDEFINED_LEVEL) {
            return ExpConfig.levelUpExp;
        }

        return RemainingExpAttribute.getRemainingExp(player);
    }

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
     * @return この経験値獲得でレベルアップしたときtrue
     */
    public static boolean add(ServerPlayer player, double amount) {
        // まだレベルシステムに参加していないプレイヤーは、最初の経験値を設定
        if (LevelAttribute.getLevel(player) <= Level.UNDEFINED_LEVEL) {
            LevelAttribute.setLevel(player, 1);
            RoleLeveledUpTimes.resetMain(player);
            RoleLeveledUpTimes.resetSub(player);
            RemainingExpAttribute.setRemainingExp(player, ExpConfig.levelUpExp - amount);
            return false;
        }

        double remaining = RemainingExpAttribute.getRemainingExp(player);
        double newRemaining = remaining - amount;

        boolean leveledUp = false;
        // レベルアップ処理
        if (newRemaining < 0) {
            // レベルインクリメント
            int newLevel = LevelAttribute.getLevel(player) + 1;
            LevelAttribute.setLevel(player, newLevel);
            // 次のレベルは現在のレベルよりたくさんの経験値が必要
            double nextLevelExp = getNextLevelExp(newLevel);
            // 今回の経験値増分を考慮して足し算
            newRemaining += nextLevelExp;

            leveledUp = true;
        }

        RemainingExpAttribute.setRemainingExp(player, newRemaining);
        return leveledUp;
    }

    /**
     * プレイヤーの経験値を初期化する
     *
     * @param player プレイヤー。
     */
    public static void clear(ServerPlayer player) {
        RemainingExpAttribute.setRemainingExp(player, getNextLevelExp(LevelAttribute.getLevel(player)));
    }
    
    /**
     * 仮にプレイヤーの経験値を初期化した場合、失われると考えられる、今までに取得した経験値量を計算する。
     * 
     * @param player プレイヤー。
     * @return 経験値量。
     */
    public static double getExpMayBeLostToClear(Player player) {
        return getNextLevelExp(Level.get(player)) - get(player);
    }

    /**
     * 次のレベルに達するのに必要な経験値量を計算する
     *
     * @param level 現在のレベル
     * @return 現在のレベルになったばかりの場合、次のレベルになるのに必要な経験値
     */
    public static double getNextLevelExp(int level) {
        return ExpConfig.levelUpExp * Math.pow(ExpConfig.levelUpIncreaseMultiplier, level - 1);
    }
}
