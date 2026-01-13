package com.github.godhexagon.oneslotsurvival.object.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
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
                Identifier.fromNamespaceAndPath("oneslotsurvival", "warrior_defensive"));
        
        /**
         * FISHERサブロールの釣り竿。
         */
        public static final TagKey<Item> FISHING_RODS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "fishing_rods"));

        /**
         * ARCHERサブロールの遠距離武器（弓、クロスボウ）。
         */
        public static final TagKey<Item> ARCHER_WEAPONS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "ranged_weapons"));

        /**
         * バケツ類（水入りバケツ、溶岩バケツ等）。
         */
        public static final TagKey<Item> BUCKETS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "buckets"));

        /**
         * ハサミ。
         */
        public static final TagKey<Item> SHEARS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "shears"));

        /**
         * BUILDERロール用：シャベル・ハサミ・剣・クワが適性のブロック。
         */
        public static final TagKey<Item> BUILDER_SOFT_BLOCKS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "builder_soft_blocks"));

        /**
         * BUILDERロール用：つるはしが適性のブロック。
         */
        public static final TagKey<Item> BUILDER_HARD_BLOCKS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "builder_hard_blocks"));

        /**
         * BUILDERロール用：斧が適性のブロック。
         */
        public static final TagKey<Item> BUILDER_WOOD_BLOCKS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "builder_wood_blocks"));

        /**
         * MINERロール用：つるはしまたはシャベル（スロット2用）。
         */
        public static final TagKey<Item> MINER_DIGGING_TOOLS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "miner_digging_tools"));

        /**
         * WARRIORロール用：剣、トライデント、メイス（スロット1用）。
         */
        public static final TagKey<Item> WARRIOR_MELEE_WEAPONS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "warrior_melee_weapons"));

        /**
         * SURVIVORロール用：斧、はさみ、クワ（スロット2用）。
         */
        public static final TagKey<Item> SURVIVOR_UTILITY_TOOLS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "survivor_utility_tools"));

        /**
         * THROWERサブロール用：投擲物（卵、雪玉、エンパ、エンダーアイ、スプラッシュポーション、残留ポーション、ウインドチャージ）。
         */
        public static final TagKey<Item> THROWABLES =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "throwables"));

        /**
         * PHARMACISTサブロール用：ポーションとシチュー。
         */
        public static final TagKey<Item> PHARMACIST_ITEMS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "pharmacist_items"));

        /**
         * ARMORERサブロール用：火打ち石と打ち金、防具、鍛冶型。
         */
        public static final TagKey<Item> ARMORER_ITEMS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "armorer_items"));

        /**
         * SCHOLARサブロール用：本、地図、模様、コンパス、時計。
         */
        public static final TagKey<Item> SCHOLAR_DOCUMENTATION =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "scholar_documentation"));

        /**
         * SCHOLARサブロール用：ブラシ、望遠鏡。
         */
        public static final TagKey<Item> SCHOLAR_INSTRUMENTS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "scholar_instruments"));

        /**
         * BREEDERサブロール用：サドル、ハーネス、馬鎧、オオカミの鎧、ヤギの角笛。
         */
        public static final TagKey<Item> BREEDER_EQUIPMENT =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "breeder_equipment"));

        /**
         * ARMORERサブロール用：火打ち石と打ち金。
         */
        public static final TagKey<Item> FLINT_AND_STEELS =
            TagKey.create(Registries.ITEM,
                Identifier.fromNamespaceAndPath("oneslotsurvival", "flint_and_steels"));
    }
}
