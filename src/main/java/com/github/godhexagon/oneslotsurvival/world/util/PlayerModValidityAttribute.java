package com.github.godhexagon.oneslotsurvival.world.util;

import com.github.godhexagon.oneslotsurvival.world.attribute.ModAttributes;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

public class PlayerModValidityAttribute {
    /**
     * Attribute Holder を安全に取得し、登録されていない場合は明確な例外をスロー
     *
     * @return MOD_ENABLED Attribute Holder
     * @throws IllegalStateException Attribute が登録されていない場合
     */
    private static Holder<Attribute> getAttributeHolder() {
        return ModAttributes.MOD_ENABLED.getHolder().orElseThrow(
                () -> new IllegalStateException(
                        "MOD_ENABLED attribute is not registered. " +
                                "This indicates a mod initialization error."
                )
        );
    }

    /**
     * 指定されたプレイヤーに対して One Slot モード有効性のワールド設定をチェック
     * クライアントとサーバーの両側で動作 - 自動的に同期される
     *
     * @param player チェックするプレイヤー
     * @return このプレイヤーに対して One Slot モードが有効な場合は true
     * @throws IllegalStateException MOD_ENABLED Attribute が登録されていない場合
     */
    public static boolean getEnabled(Player player) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            // このプレイヤーエンティティに Attribute が存在しない（EntityAttributeModificationEvent 後は発生しないはず）
            return false;
        }
        // 値 > 0.5 は有効を意味する（浮動小数点の安全性のため 0.5 を閾値として使用）
        return attribute.getBaseValue() > 0.5;
    }

    /**
     * 指定されたプレイヤーに対して One Slot モードを有効または無効にする
     * 有効化時は、既存のインベントリアイテムを処理
     * 自動的に永続化され、クライアントと同期される
     *
     * @param player 変更するプレイヤー
     * @param enabled One Slot モードを有効にする場合は true、無効にする場合は false
     * @throws IllegalStateException MOD_ENABLED Attribute が登録されていないか、プレイヤーに存在しない場合
     */
    public static void updateEnabled(ServerPlayer player, boolean enabled) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            throw new IllegalStateException(
                    "Player does not have MOD_ENABLED attribute. " +
                            "This indicates EntityAttributeModificationEvent was not properly handled."
            );
        }

        // 基本値を設定: 有効の場合は 1.0、無効の場合は 0.0
        attribute.setBaseValue(enabled ? 1.0 : 0.0);

        if (enabled) {
            SlotBarrierFilling.fillUp(player);
        } else {
            SlotBarrierFilling.clean(player);
        }
    }
}
