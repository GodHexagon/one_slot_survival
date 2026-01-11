package com.github.godhexagon.oneslotsurvival.rule.role;

import com.github.godhexagon.oneslotsurvival.world.storage.GameData;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerProgress;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Random;

/**
 * ロール配布機能を提供するユーティリティクラス。
 *
 * <p>以下の3つの配布方式をサポートします:</p>
 * <ul>
 *   <li>random: 完全ランダムにロールを割り当て</li>
 *   <li>definedList: 定義済みリストから順番に割り当て（呼び出し回数を永続化）</li>
 * </ul>
 */
public class RoleDistribution {

    /**
     * 定義済みのメインロールリスト（繰り返し使用）
     */
    private static final List<MainRole> DEFINED_MAIN_ROLES = List.of(
        MainRole.MINER,
        MainRole.WARRIOR,
        MainRole.SURVIVOR,
        MainRole.BUILDER
    );

    /**
     * 定義済みのサブロールリスト（繰り返し使用）
     */
    private static final List<SubRole> DEFINED_SUB_ROLES = List.of(
        SubRole.ARMORER,
        SubRole.ARCHER,
        SubRole.FISHER,
        SubRole.BREEDER,
        SubRole.SCHOLAR,
        SubRole.PHARMACIST,
        SubRole.THROWER
    );

    /**
     * プレイヤーにランダムなロールを割り当てます。
     *
     * @param player 対象プレイヤー
     */
    public static void setRandom(ServerPlayer player) {
        Random random = new Random();

        // 有効なメインロール（ERROR, UNASSIGNEDを除く）
        List<MainRole> validMainRoles = List.of(
            MainRole.MINER,
            MainRole.WARRIOR,
            MainRole.SURVIVOR,
            MainRole.BUILDER
        );

        // 有効なサブロール（ERROR, UNASSIGNEDを除く）
        List<SubRole> validSubRoles = List.of(
            SubRole.ARMORER,
            SubRole.ARCHER,
            SubRole.FISHER,
            SubRole.BREEDER,
            SubRole.SCHOLAR,
            SubRole.PHARMACIST,
            SubRole.THROWER
        );

        // ランダムにメインロールを選択
        MainRole mainRole = validMainRoles.get(random.nextInt(validMainRoles.size()));
        SubRole subRole = validSubRoles.get(random.nextInt(validSubRoles.size()));

        // ロールを設定（プログレスリセット付き）
        PlayerProgress.changeRole(player, mainRole);
        PlayerProgress.changeRole(player, subRole);
    }

    /**
     * 定義済みリストに基づいてロールを割り当てます。
     *
     * <p>呼び出し回数をカウントし、リストを循環的に使用します。
     * カウンタは永続化されるため、ワールド再起動後も継続します。</p>
     *
     * @param player 対象プレイヤー
     */
    public static void setDefinedListWithCounting(ServerPlayer player) {
        // 現在のカウンタを取得
        int callCount = GameData.getDefinedListCallCount(player.getServer());

        setDefinedList(player, callCount);

        // カウンタをインクリメント
        GameData.incrementDefinedListCallCount(player.getServer());
    }
    
    /**
     * 定義済みリストに基づいてロールを割り当てます。
     *
     * @param player 対象プレイヤー
     */
    public static void setDefinedList(ServerPlayer player, int callCount) {
        // メインロールをリストから循環的に取得
        MainRole mainRole = DEFINED_MAIN_ROLES.get(callCount % DEFINED_MAIN_ROLES.size());
        // サブロールをリストから循環的に取得
        SubRole subRole = DEFINED_SUB_ROLES.get(callCount % DEFINED_SUB_ROLES.size());

        // ロールを設定（プログレスリセット付き）
        PlayerProgress.changeRole(player, mainRole);
        PlayerProgress.changeRole(player, subRole);
    }
}
