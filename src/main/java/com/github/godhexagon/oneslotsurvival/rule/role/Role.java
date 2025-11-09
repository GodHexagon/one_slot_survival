package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * メインロールとサブロールの共通インターフェース
 */
public interface Role {
    
    /**
     * ロールIDを取得
     *
     * @return ロールID
     */
    int getAttributeId();

    /**
     * コマンド名を取得
     *
     * @return コマンド名
     */
    String getCommandName();

    /**
     * 翻訳可能な表示名を取得
     *
     * @return 翻訳キーを含むComponent
     */
    Component getDisplayName();

    /**
     * このロールで使用可能なアイテムタグのリストを取得
     * <p>
     * インデックスはロールスロットのインデックスに対応します。
     * </p>
     *
     * @return アイテムタグのリスト（スロット順）
     */
    List<TagKey<Item>> getSlotItemTags();

    /**
     * このロールがロールスロットを持つかどうか
     *
     * @return ロールスロットを持つ場合 true
     */
    boolean hasRoleSlots();
}
