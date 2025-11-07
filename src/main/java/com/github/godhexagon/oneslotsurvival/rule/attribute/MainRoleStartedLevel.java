package com.github.godhexagon.oneslotsurvival.rule.attribute;

import com.github.godhexagon.oneslotsurvival.object.attribute.ModAttributes;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class MainRoleStartedLevel {
    /**
     * Attribute Holder を安全に取得し、登録されていない場合は明確な例外をスロー
     *
     * @return MAIN_ROLE_LEVEL Attribute Holder
     * @throws IllegalStateException Attribute が登録されていない場合
     */
    private static Holder<Attribute> getAttributeHolder() {
        return ModAttributes.MAIN_ROLE_STARTED_LEVEL.getHolder().orElseThrow(
                () -> new IllegalStateException(
                        "MAIN_ROLE_STARTED_LEVEL attribute is not registered. " +
                                "This indicates a mod initialization error."
                )
        );
    }

    /**
     * 指定されたプレイヤーのメインロールレベルを取得
     * クライアントとサーバーの両側で動作 - 自動的に同期される
     *
     * @param player チェックするプレイヤー
     * @return プレイヤーのメインロールレベル、デフォルトは 1
     * @throws IllegalStateException MAIN_ROLE_LEVEL Attribute が登録されていない場合
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
     * 指定されたプレイヤーのメインロールレベルを設定
     * 自動的に永続化され、クライアントと同期される
     *
     * @param player 変更するプレイヤー
     * @param level 設定するレベル（1以上）
     * @throws IllegalStateException MAIN_ROLE_LEVEL Attribute が登録されていないか、プレイヤーに存在しない場合
     */
    public static void set(ServerPlayer player, int level) {

        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            throw new IllegalStateException(
                    "Player does not have MAIN_ROLE_STARTED_LEVEL attribute. " +
                            "This indicates EntityAttributeModificationEvent was not properly handled."
            );
        }

        attribute.setBaseValue(level);
    }
}
