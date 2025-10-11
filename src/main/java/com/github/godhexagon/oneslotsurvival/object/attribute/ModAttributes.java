package com.github.godhexagon.oneslotsurvival.object.attribute;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Custom attributes for One Slot Survival mod.
 */
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, OneSlotSurvivalMod.MODID);

    /**
     * Attribute representing whether One Slot mode is enabled for a player.
     * Value: 0.0 = disabled, 1.0 = enabled
     */
    public static final RegistryObject<Attribute> MOD_ENABLED = ATTRIBUTES.register(
        "mod_enabled",
        () -> new RangedAttribute(
            "attribute.oneslotsurvival.mod_enabled",
            0.0,  // default value (disabled). ワールド設定によるデフォルト変更は後で実装.
            0.0,  // min value
            1.0   // max value
        ).setSyncable(true)  // Sync to client automatically
    );
}
