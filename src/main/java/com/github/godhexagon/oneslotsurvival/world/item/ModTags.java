package com.github.godhexagon.oneslotsurvival.world.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * このMODで定義するカスタムアイテムタグ。
 */
public class ModTags {
    public static class Items {
        /**
         * WARRIORロールの防御的なアイテム（盾、火打ち石と打ち金）。
         */
        public static final TagKey<Item> WARRIOR_DEFENSIVE =
            TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "warrior_defensive"));

        /**
         * WARRIORロールの特殊武器（トライデント、メイス）。
         */
        public static final TagKey<Item> WARRIOR_SPECIAL_WEAPONS =
            TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("oneslotsurvival", "warrior_special_weapons"));
    }
}
