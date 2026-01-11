package com.github.godhexagon.oneslotsurvival.rule.attribute;

import com.github.godhexagon.oneslotsurvival.object.attribute.ModAttributes;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class Initialized {
    /**
     * Attribute Holder を安全に取得し、登録されていない場合は明確な例外をスロー
     *
     * @return INITIALIZED Attribute Holder
     * @throws IllegalStateException Attribute が登録されていない場合
     */
    private static Holder<Attribute> getAttributeHolder() {
        return ModAttributes.INITIALIZED.getHolder().orElseThrow(
                () -> new IllegalStateException(
                        "INITIALIZED attribute is not registered. " +
                                "This indicates a mod initialization error."
                )
        );
    }

    /**
     * 指定されたプレイヤーが初期化済みかどうかをチェック
     * クライアントとサーバーの両側で動作 - 自動的に同期される
     *
     * @param player チェックするプレイヤー
     * @return このプレイヤーが初期化済みの場合は true
     * @throws IllegalStateException INITIALIZED Attribute が登録されていない場合
     */
    public static boolean getInitialized(Player player) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            // このプレイヤーエンティティに Attribute が存在しない（EntityAttributeModificationEvent 後は発生しないはず）
            return false;
        }
        // 値 > 0.5 は初期化済みを意味する（浮動小数点の安全性のため 0.5 を閾値として使用）
        return attribute.getBaseValue() > 0.5;
    }

    /**
     * 指定されたプレイヤーの初期化状態を更新する
     * 自動的に永続化され、クライアントと同期される
     *
     * @param player 変更するプレイヤー
     * @param initialized 初期化済みにする場合は true、未初期化にする場合は false
     * @throws IllegalStateException INITIALIZED Attribute が登録されていないか、プレイヤーに存在しない場合
     */
    public static void updateInitialized(ServerPlayer player, boolean initialized) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            throw new IllegalStateException(
                    "Player does not have INITIALIZED attribute. " +
                            "This indicates EntityAttributeModificationEvent was not properly handled."
            );
        }

        // 基本値を設定: 初期化済みの場合は 1.0、未初期化の場合は 0.0
        attribute.setBaseValue(initialized ? 1.0 : 0.0);
    }
}
