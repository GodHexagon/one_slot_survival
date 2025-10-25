package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.util.level.Exp;
import com.github.godhexagon.oneslotsurvival.world.util.player.PlayerModValidity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * ServerPlayerクラスに経験値獲得機能を追加するMixin
 * バニラの統計情報システムをフックし、プレイヤーのアクションに応じて経験値を付与します。
 *
 * <h2>統計情報の単位について</h2>
 * バニラの統計情報(Stats)は、StatFormatterによって単位が決まっています:
 * <ul>
 *   <li><b>StatFormatter.DISTANCE</b>: cm単位で記録される (例: WALK_ONE_CM, SPRINT_ONE_CM)
 *       <br>→ 100cm = 1ブロック = 1メートル</li>
 *   <li><b>StatFormatter.DIVIDE_BY_TEN</b>: 実際の値の10倍で記録される (例: DAMAGE_DEALT, DAMAGE_BLOCKED_BY_SHIELD)
 *       <br>→ amount 50 = 実際のダメージ 5.0</li>
 *   <li><b>StatFormatter.DEFAULT</b>: そのままの値で記録される (例: JUMP, MOB_KILLS)
 *       <br>→ amount 1 = 1回/1体</li>
 *   <li><b>StatFormatter.TIME</b>: tick単位で記録される (例: PLAY_TIME)
 *       <br>→ 20 tick = 1秒</li>
 * </ul>
 *
 * 詳細は以下を参照:
 * <ul>
 *   <li>{@code net.minecraft.stats.Stats} - 統計情報の定義</li>
 *   <li>{@code net.minecraft.stats.StatFormatter} - 単位の定義</li>
 *   <li>{@code net.minecraft.server.level.ServerPlayer#doServerTick} - 移動距離の記録処理</li>
 * </ul>
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    /**
     * バニラの経験値獲得をインターセプトし、獲得した経験値に応じてメインロールの経験値を増やします。
     *
     * @param experiencePoints 獲得する経験値ポイント
     * @param ci コールバック情報
     */
    @Inject(method = "giveExperiencePoints", at = @At("HEAD"))
    private void onGiveExperiencePoints(int experiencePoints, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        // MODが有効なプレイヤーのみ処理
        if (!PlayerModValidity.isEffective(player)) {
            return;
        }

        // 経験値ポイントが0以下の場合は処理しない
        if (experiencePoints <= 0) {
            return;
        }

        // バニラの経験値1ポイント = メインロールの経験値1.0として加算
        Exp.addMain(player, (double) experiencePoints);
    }

    /**
     * バニラの統計情報更新をインターセプトし、統計情報に応じて経験値を付与します。
     *
     * @param stat 更新される統計情報
     * @param amount 統計情報の増加量
     * @param ci コールバック情報
     */
    @Inject(method = "awardStat", at = @At("TAIL"))
    private void onAwardStat(Stat<?> stat, int amount, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        // MODが有効なプレイヤーのみ処理
        if (!PlayerModValidity.isEffective(player)) {
            return;
        }

        double expToAdd = 0.0;

        // 統計情報に応じて経験値を計算
        if (stat == Stats.CUSTOM.get(Stats.WALK_ONE_CM)) {
            // amountの意味: 歩行距離(cm単位)。100cm = 1ブロック
            expToAdd = amount * 0.0001;

        } else if (stat == Stats.CUSTOM.get(Stats.SPRINT_ONE_CM)) {
            // amountの意味: 走行距離(cm単位)。100cm = 1ブロック
            expToAdd = amount * 0.0001;

        } else if (stat == Stats.CUSTOM.get(Stats.CROUCH_ONE_CM)) {
            // amountの意味: しゃがみ歩行距離(cm単位)。100cm = 1ブロック
            expToAdd = amount * 0.0002;

        } else if (stat == Stats.CUSTOM.get(Stats.FLY_ONE_CM)) {
            // amountの意味: 空中移動距離(cm単位)。100cm = 1ブロック
            expToAdd = amount * 0.0001;

        } else if (stat == Stats.CUSTOM.get(Stats.DAMAGE_DEALT)) {
            // amountの意味: 与えたダメージ量の10倍 (例: 5.0ダメージ = amount 50)
            expToAdd = amount * 0.002;

        } else if (stat == Stats.CUSTOM.get(Stats.DAMAGE_TAKEN)) {
            // amountの意味: 受けたダメージ量の10倍 (例: 5.0ダメージ = amount 50)
            expToAdd = amount * 0.05;

        } else if (stat == Stats.CUSTOM.get(Stats.DAMAGE_BLOCKED_BY_SHIELD)) {
            // amountの意味: 盾で防いだダメージ量の10倍 (例: 5.0ダメージ = amount 50)
            expToAdd = amount * 0.05;
        }

        // 経験値を加算
        if (expToAdd > 0) {
            Exp.addMain(player, expToAdd);
        }
    }
}
