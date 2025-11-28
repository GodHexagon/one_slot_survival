package com.github.godhexagon.oneslotsurvival.object.gamerule;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;

import net.minecraft.world.level.GameRules;

/**
 * One Slot Survival MOD用のカスタムGameRuleを管理するクラス。
 *
 * <p>バニラのGameRulesシステムを活用して、ワールドごとの設定を永続化します。</p>
 *
 * <h2>使用方法</h2>
 * <pre>{@code
 * // ゲーム内コマンド
 * /gamerule oneSlotRoleChanging false
 *
 * // コード内からのアクセス
 * boolean canChange = level.getGameRules().getBoolean(ModGameRules.ROLE_CHANGING);
 * }</pre>
 */
public class ModGameRules {

    /**
     * プレイヤーが自分のメインロールを変更できるかどうかを制御するGameRule。
     *
     * <ul>
     *   <li>デフォルト値: true（変更可能）</li>
     *   <li>カテゴリ: PLAYER</li>
     * </ul>
     */
    public static GameRules.Key<GameRules.BooleanValue> MAIN_ROLE_CHANGING;

    /**
     * プレイヤーが自分のサブロールを変更できるかどうかを制御するGameRule。
     *
     * <ul>
     *   <li>デフォルト値: true（変更可能）</li>
     *   <li>カテゴリ: PLAYER</li>
     * </ul>
     */
    public static GameRules.Key<GameRules.BooleanValue> SUB_ROLE_CHANGIN;

    /**
     * カスタムGameRuleを登録します。
     *
     * <p>このメソッドはMODの初期化時に一度だけ呼ばれる必要があります。</p>
     */
    public static void register() {
        MAIN_ROLE_CHANGING = GameRules.register(
            "mainRoleChanging/" + OneSlotSurvivalMod.MODID,
            GameRules.Category.PLAYER,
            GameRules.BooleanValue.create(true)
        );
        SUB_ROLE_CHANGIN = GameRules.register(
            "subRoleChanging/" + OneSlotSurvivalMod.MODID,
            GameRules.Category.PLAYER,
            GameRules.BooleanValue.create(true)
        );
    }
}
