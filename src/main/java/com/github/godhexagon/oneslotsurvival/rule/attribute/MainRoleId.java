package com.github.godhexagon.oneslotsurvival.rule.attribute;

import com.github.godhexagon.oneslotsurvival.data.attribute.ModAttributes;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class MainRoleId {
    /**
     * Attribute Holder を安全に取得し、登録されていない場合は明確な例外をスロー
     *
     * @return MAIN_ROLE_ID Attribute Holder
     * @throws IllegalStateException Attribute が登録されていない場合
     */
    private static Holder<Attribute> getAttributeHolder() {
        return ModAttributes.MAIN_ROLE_ID.getHolder().orElseThrow(
                () -> new IllegalStateException(
                        "MAIN_ROLE_ID attribute is not registered. " +
                                "This indicates a mod initialization error."
                )
        );
    }

    /**
     * 指定されたプレイヤーのメインロールIDを取得
     * クライアントとサーバーの両側で動作 - 自動的に同期される
     *
     * @param player チェックするプレイヤー
     * @return プレイヤーのメインロールID、未割り当ての場合は UNASSIGNED (-1)
     * @throws IllegalStateException MAIN_ROLE_ID Attribute が登録されていない場合
     */
    public static int getRoleId(Player player) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            // フォールバック処理
            // このプレイヤーエンティティに Attribute が存在しない（EntityAttributeModificationEvent 後は発生しないはず）
            return -1;
        }
        return (int) attribute.getBaseValue();
    }

    /**
     * 指定されたプレイヤーにメインロールIDを設定
     * 自動的に永続化され、クライアントと同期される
     *
     * @param player 変更するプレイヤー
     * @param roleId 設定するロールID（-1 の場合は未割り当て）
     * @throws IllegalStateException MAIN_ROLE_ID Attribute が登録されていないか、プレイヤーに存在しない場合
     */
    public static void setRoleId(ServerPlayer player, int roleId) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            throw new IllegalStateException(
                    "Player does not have MAIN_ROLE_ID attribute. " +
                            "This indicates EntityAttributeModificationEvent was not properly handled."
            );
        }

        attribute.setBaseValue(roleId);
    }
}
