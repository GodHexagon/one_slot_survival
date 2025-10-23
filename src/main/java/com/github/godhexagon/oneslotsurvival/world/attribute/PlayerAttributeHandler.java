package com.github.godhexagon.oneslotsurvival.world.attribute;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

/**
 * プレイヤーにカスタム Attribute を追加するためのイベントハンドラ
 */
public class PlayerAttributeHandler {

    /**
     * プレイヤーエンティティにカスタム Attribute を追加
     * このイベントは起動時に mod イベントバスで発火される
     *
     * @param event エンティティ Attribute 変更イベント
     * @throws IllegalStateException MOD_ENABLED または MAIN_ROLE_ID Attribute が登録されていない場合
     */
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(
            EntityType.PLAYER,
            ModAttributes.MOD_ENABLED.getHolder().orElseThrow(
                () -> new IllegalStateException(
                    "MOD_ENABLED attribute is not registered. " +
                    "This indicates an issue with DeferredRegister initialization."
                )
            )
        );
        event.add(
            EntityType.PLAYER,
            ModAttributes.MAIN_ROLE_ID.getHolder().orElseThrow(
                () -> new IllegalStateException(
                    "MAIN_ROLE_ID attribute is not registered. " +
                    "This indicates an issue with DeferredRegister initialization."
                )
            )
        );
    }
}
