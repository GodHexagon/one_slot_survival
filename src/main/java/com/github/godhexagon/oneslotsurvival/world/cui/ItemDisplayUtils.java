package com.github.godhexagon.oneslotsurvival.world.cui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * アイテム表示のためのユーティリティクラス
 * <p>
 * UI層で使用されるアイテム関連の表示処理を提供。
 * </p>
 */
public class ItemDisplayUtils {

    /**
     * タグから例示用のアイテム名（翻訳済み）リストを取得
     *
     * @param tag      アイテムタグ
     * @param maxCount 最大取得数
     * @return アイテムの表示名リスト
     */
    public static List<Component> getExampleItems(TagKey<Item> tag, int maxCount) {
        List<Component> items = new ArrayList<>();

        BuiltInRegistries.ITEM.stream()
            .filter(item -> BuiltInRegistries.ITEM.wrapAsHolder(item).is(tag))
            .limit(maxCount)
            .forEach(item -> {
                ItemStack stack = new ItemStack(item);
                Component displayName = stack.getHoverName();
                items.add(displayName);
            });

        return items;
    }

    /**
     * タグ内の総アイテム数を取得
     *
     * @param tag アイテムタグ
     * @return タグに含まれるアイテムの総数
     */
    public static int getTotalItemsInTag(TagKey<Item> tag) {
        return (int) BuiltInRegistries.ITEM.stream()
            .filter(item -> BuiltInRegistries.ITEM.wrapAsHolder(item).is(tag))
            .count();
    }
}
