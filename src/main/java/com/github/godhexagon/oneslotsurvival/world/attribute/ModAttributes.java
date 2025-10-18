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
}
