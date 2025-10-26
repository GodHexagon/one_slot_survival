package com.github.godhexagon.oneslotsurvival.world.util.attribute;

import com.github.godhexagon.oneslotsurvival.world.attribute.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

/**
 * プレイヤーのメインロールレベルアップに必要な残り経験値を管理するユーティリティクラス
 */
public class MainRoleRemainingExp {
    /**
     * Attribute Holder を安全に取得し、登録されていない場合は明確な例外をスロー
     *
     * @return MAIN_ROLE_REMAINING_EXP Attribute Holder
     * @throws IllegalStateException Attribute が登録されていない場合
     */
    private static Holder<Attribute> getAttributeHolder() {
        return ModAttributes.MAIN_ROLE_REMAINING_EXP.getHolder().orElseThrow(
                () -> new IllegalStateException(
                        "MAIN_ROLE_REMAINING_EXP attribute is not registered. " +
                                "This indicates a mod initialization error."
                )
        );
    }

    /**
     * 指定されたプレイヤーのメインロールレベルアップに必要な残り経験値を取得
     * クライアントとサーバーの両側で動作 - 自動的に同期される
     *
     * @param player チェックするプレイヤー
     * @return プレイヤーのメインロールレベルアップに必要な残り経験値、デフォルトは 0.0
     * @throws IllegalStateException MAIN_ROLE_REMAINING_EXP Attribute が登録されていない場合
     */
    public static double getRemainingExp(Player player) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            // フォールバック処理
            // このプレイヤーエンティティに Attribute が存在しない（EntityAttributeModificationEvent 後は発生しないはず）
            return 0.0;
        }
        return attribute.getBaseValue();
    }

    /**
     * 指定されたプレイヤーのメインロールレベルアップに必要な残り経験値を設定
     * 自動的に永続化され、クライアントと同期される
     *
     * @param player 変更するプレイヤー
     * @param remainingExp 設定する残り経験値
     * @throws IllegalStateException MAIN_ROLE_REMAINING_EXP Attribute が登録されていないか、プレイヤーに存在しない場合
     */
    public static void setRemainingExp(ServerPlayer player, double remainingExp) {

        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            throw new IllegalStateException(
                    "Player does not have MAIN_ROLE_REMAINING_EXP attribute. " +
                            "This indicates EntityAttributeModificationEvent was not properly handled."
            );
        }

        attribute.setBaseValue(remainingExp);
    }
}
