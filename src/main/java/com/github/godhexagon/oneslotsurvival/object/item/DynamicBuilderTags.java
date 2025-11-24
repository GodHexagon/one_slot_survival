package com.github.godhexagon.oneslotsurvival.object.item;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/**
 * Builderロール用の動的タグ生成システム
 * <p>
 * 実行時にすべてのブロックを走査し、各ツールの採掘速度を確認して、
 * どのブロックがどのツールで効率的に採掘できるかを動的に判定する。
 * </p>
 */
public class DynamicBuilderTags {

    private static Set<Block> softBlocks = new HashSet<>();
    private static Set<Block> hardBlocks = new HashSet<>();
    private static Set<Block> woodBlocks = new HashSet<>();
    private static Set<Item> softItems = new HashSet<>();
    private static Set<Item> hardItems = new HashSet<>();
    private static Set<Item> woodItems = new HashSet<>();
    private static boolean initialized = false;

    /**
     * 動的タグを生成する
     *
     * @param registries レジストリアクセス
     */
    public static void generateBuilderTags(RegistryAccess registries) {
        if (initialized) {
            return; // 既に初期化済み
        }

        Registry<Block> blockRegistry = registries.lookupOrThrow(Registries.BLOCK);

        softBlocks.clear();
        hardBlocks.clear();
        woodBlocks.clear();
        softItems.clear();
        hardItems.clear();
        woodItems.clear();

        // すべてのブロックを走査
        for (Block block : blockRegistry) {
            BlockState defaultState = block.defaultBlockState();

            // シャベル/ハサミ/剣/クワで効率的に採掘できるか？
            if (isFastWithShovel(defaultState)
                    || isFastWithShears(defaultState)
                    || isFastWithSword(defaultState)
                    || isFastWithHoe(defaultState)) {
                softBlocks.add(block);
                // BlockItemを取得して追加
                Item blockItem = block.asItem();
                if (blockItem != Items.AIR) {
                    softItems.add(blockItem);
                }
            }

            // つるはしで効率的に採掘できるか？
            if (isFastWithPickaxe(defaultState)) {
                hardBlocks.add(block);
                // BlockItemを取得して追加
                Item blockItem = block.asItem();
                if (blockItem != Items.AIR) {
                    hardItems.add(blockItem);
                }
            }

            // 斧で効率的に採掘できるか？
            if (isFastWithAxe(defaultState)) {
                woodBlocks.add(block);
                // BlockItemを取得して追加
                Item blockItem = block.asItem();
                if (blockItem != Items.AIR) {
                    woodItems.add(blockItem);
                }
            }
        }

        initialized = true;
    }

    /**
     * ブロックがつるはしで効率的に採掘できるかを判定
     */
    private static boolean isFastWithPickaxe(BlockState state) {
        return isFastWithTool(Items.DIAMOND_PICKAXE, state);
    }

    /**
     * ブロックが斧で効率的に採掘できるかを判定
     */
    private static boolean isFastWithAxe(BlockState state) {
        return isFastWithTool(Items.DIAMOND_AXE, state);
    }

    /**
     * ブロックがシャベルで効率的に採掘できるかを判定
     */
    private static boolean isFastWithShovel(BlockState state) {
        return isFastWithTool(Items.DIAMOND_SHOVEL, state);
    }

    /**
     * ブロックがハサミで効率的に採掘できるかを判定
     */
    private static boolean isFastWithShears(BlockState state) {
        return isFastWithTool(Items.SHEARS, state);
    }

    /**
     * ブロックが剣で効率的に採掘できるかを判定
     */
    private static boolean isFastWithSword(BlockState state) {
        return isFastWithTool(Items.DIAMOND_SWORD, state);
    }

    /**
     * ブロックがクワで効率的に採掘できるかを判定
     */
    private static boolean isFastWithHoe(BlockState state) {
        return isFastWithTool(Items.DIAMOND_HOE, state);
    }

    /**
     * 指定されたツールでブロックを効率的に採掘できるかを判定
     *
     * @param toolItem ツールアイテム
     * @param state ブロックステート
     * @return 基本速度より速く採掘できる場合true
     */
    private static boolean isFastWithTool(Item toolItem, BlockState state) {
        ItemStack toolStack = new ItemStack(toolItem);
        Tool tool = toolStack.get(DataComponents.TOOL);
        if (tool == null) {
            return false;
        }

        // 採掘速度が基本速度（1.0f）より速いか確認
        float miningSpeed = tool.getMiningSpeed(state);
        return miningSpeed > 1.0f;
    }

    /**
     * Soft Blocksのセットを取得
     *
     * @return シャベル/ハサミ/剣/クワで効率的に採掘できるブロックのセット
     */
    public static Set<Block> getSoftBlocks() {
        return new HashSet<>(softBlocks);
    }

    /**
     * Hard Blocksのセットを取得
     *
     * @return つるはしで効率的に採掘できるブロックのセット
     */
    public static Set<Block> getHardBlocks() {
        return new HashSet<>(hardBlocks);
    }

    /**
     * Wood Blocksのセットを取得
     *
     * @return 斧で効率的に採掘できるブロックのセット
     */
    public static Set<Block> getWoodBlocks() {
        return new HashSet<>(woodBlocks);
    }

    /**
     * 初期化済みかどうかを確認
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * 指定されたアイテムがSoft Blocksに含まれるか判定
     *
     * @param item 判定するアイテム
     * @return Soft Blocksに含まれる場合true
     */
    public static boolean isSoftBlockItem(Item item) {
        return softItems.contains(item);
    }

    /**
     * 指定されたアイテムがHard Blocksに含まれるか判定
     *
     * @param item 判定するアイテム
     * @return Hard Blocksに含まれる場合true
     */
    public static boolean isHardBlockItem(Item item) {
        return hardItems.contains(item);
    }

    /**
     * 指定されたアイテムがWood Blocksに含まれるか判定
     *
     * @param item 判定するアイテム
     * @return Wood Blocksに含まれる場合true
     */
    public static boolean isWoodBlockItem(Item item) {
        return woodItems.contains(item);
    }
}
