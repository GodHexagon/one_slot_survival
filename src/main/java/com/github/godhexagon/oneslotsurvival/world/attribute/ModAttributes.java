package com.github.godhexagon.oneslotsurvival.world.attribute;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * One Slot Survival mod のカスタム Attribute
 */
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, OneSlotSurvivalMod.MODID);

    /**
     * プレイヤーに対して One Slot モードが有効かどうかを表す Attribute
     * 値: 0.0 = 無効, 1.0 = 有効
     */
    public static final RegistryObject<Attribute> MOD_ENABLED = ATTRIBUTES.register(
        "mod_enabled",
        () -> new RangedAttribute(
            "attribute.oneslotsurvival.mod_enabled",
            1.0,  // TODO: 途中でMOD導入することを想定して、デフォルトは無効にする。現在はデバッグを楽にするために有効状態がデフォルト
            0.0,  // 最小値
            1.0   // 最大値
        ).setSyncable(true)  // 自動的にクライアントと同期
    );

    /**
     * プレイヤーのメインロールIDを表す Attribute
     * 値: ロールID（整数値）、-1.0 = 未割り当て
     */
    public static final RegistryObject<Attribute> MAIN_ROLE_ID = ATTRIBUTES.register(
        "main_role_id",
        () -> new RangedAttribute(
            "attribute.oneslotsurvival.main_role_id",
            0,  // デフォルト: 未割り当て
            -1.0,  // 最小値
            Double.MAX_VALUE  // 最大値（ロールIDの上限）
        ).setSyncable(true)  // 自動的にクライアントと同期
    );

    /**
     * プレイヤーのメインロールレベルを表す Attribute
     * 値: レベル（整数値）、デフォルト = 1
     */
    public static final RegistryObject<Attribute> MAIN_ROLE_LEVEL = ATTRIBUTES.register(
        "main_role_level",
        () -> new RangedAttribute(
            "attribute.oneslotsurvival.main_role_level",
            1,  // デフォルト: レベル1
            1.0,  // 最小値
            Double.MAX_VALUE  // 最大値（レベル上限）
        ).setSyncable(true)  // 自動的にクライアントと同期
    );

    /**
     * プレイヤーのメインロール経験値を表す Attribute
     * 値: 経験値（double値）、デフォルト = 0.0
     */
    public static final RegistryObject<Attribute> MAIN_ROLE_EXP = ATTRIBUTES.register(
        "main_role_exp",
        () -> new RangedAttribute(
            "attribute.oneslotsurvival.main_role_exp",
            0.0,  // デフォルト: 経験値0
            0.0,  // 最小値
            Double.MAX_VALUE  // 最大値
        ).setSyncable(true)  // 自動的にクライアントと同期
    );
}
