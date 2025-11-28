package com.github.godhexagon.oneslotsurvival.rule.attribute;

import com.github.godhexagon.oneslotsurvival.object.attribute.ModAttributes;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class SubRoleStartedLevel {
    /**
     * Attribute Holder を安全に取得し、登録されていない場合は明確な例外をスロー
     *
     * @return SUB_ROLE_STARTED_LEVEL Attribute Holder
     * @throws IllegalStateException Attribute が登録されていない場合
     */
    private static Holder<Attribute> getAttributeHolder() {
        return ModAttributes.SUB_ROLE_STARTED_LEVEL.getHolder().orElseThrow(
                () -> new IllegalStateException(
                        "SUB_ROLE_STARTED_LEVEL attribute is not registered. " +
                                "This indicates a mod initialization error."
                )
        );
    }

    /**
     * 指定されたプレイヤーのサブロール開始レベルを取得
     * クライアントとサーバーの両側で動作 - 自動的に同期される
     *
     * @param player チェックするプレイヤー
     * @return プレイヤーのサブロール開始レベル、デフォルトは 0
     * @throws IllegalStateException SUB_ROLE_STARTED_LEVEL Attribute が登録されていない場合
     */
    public static int get(Player player) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            // フォールバック処理
            // このプレイヤーエンティティに Attribute が存在しない（EntityAttributeModificationEvent 後は発生しないはず）
            return 0;
        }
        return (int) attribute.getBaseValue();
    }

    /**
     * 指定されたプレイヤーのサブロール開始レベルを設定
     * 自動的に永続化され、クライアントと同期される
     *
     * @param player 変更するプレイヤー
     * @param level 設定するレベル
     * @throws IllegalStateException SUB_ROLE_STARTED_LEVEL Attribute が登録されていないか、プレイヤーに存在しない場合
     */
    public static void set(ServerPlayer player, int level) {

        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            throw new IllegalStateException(
                    "Player does not have SUB_ROLE_STARTED_LEVEL attribute. " +
                            "This indicates EntityAttributeModificationEvent was not properly handled."
            );
        }

        attribute.setBaseValue(level);
    }
}
