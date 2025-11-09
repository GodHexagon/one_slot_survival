package com.github.godhexagon.oneslotsurvival.rule.role;

import com.github.godhexagon.oneslotsurvival.object.item.ModTags;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;

/**
 * プレイヤーのサブロール定義
 * <p>
 * サブロールはメインロールと並行して使用できる追加のロールです。
 * 各ロールはユニークなIDを持ち、プレイヤーのAttributeとして永続化される。
 * 各ロールは使用可能なアイテムタグのリストを持ち、ロールスロットの制限を定義する。
 * </p>
 */
public enum SubRole implements Role {
    /**
     * 不明な状態
     */
    ERROR(-1, "sub_role_error", Collections.emptyList()),

    /**
     * サブロール未割り当て状態
     */
    UNASSIGNED(0, "sub_role_unassigned", Collections.emptyList()),

    /**
     * 釣り特化サブロール
     * スロット1: 釣り竿
     * スロット2: ボート
     * スロット3: ボート
     */
    FISHER(1, "fisher", List.of(ModTags.Items.FISHING_RODS, ItemTags.BOATS, ItemTags.BOATS)),

    /**
     * 弓矢特化サブロール
     * スロット1: 弓またはクロスボウ
     * スロット2: 矢（通常、効果付き、光の矢）
     * スロット3: 矢（通常、効果付き、光の矢）
     */
    ARCHER(2, "archer", List.of(ModTags.Items.ARCHER_WEAPONS, ItemTags.ARROWS, ItemTags.ARROWS));

    private final int attributeId;
    private final String commandName;
    private final List<TagKey<Item>> slotItemTags;
    
    SubRole(int attribute_id, String command_name, List<TagKey<Item>> slot_item_tags) {
        this.attributeId = attribute_id;
        this.commandName = command_name;
        this.slotItemTags = slot_item_tags;
    }

    /**
     * ロールIDを取得
     *
     * @return ロールID
     */
    @Override
    public int getAttributeId() {
        return attributeId;
    }

    /**
     * コマンド名を取得
     *
     * @return コマンド名
     */
    @Override
    public String getCommandName() {
        return commandName;
    }

    /**
     * 翻訳可能な表示名を取得
     *
     * @return 翻訳キーを含むComponent
     */
    @Override
    public Component getDisplayName() {
        return Component.translatable("role.oneslotsurvival." + commandName);
    }
    
    /**
     * このロールで使用可能なアイテムタグのリストを取得
     * <p>
     * インデックスはロールスロットのインデックスに対応します。
     * 例: index 0 = 1番目のロールスロット
     * </p>
     *
     * @return アイテムタグのリスト（スロット順）
     */
    public List<TagKey<Item>> getSubRoleSlotItemTags() {
        return slotItemTags;
    }

    /**
     * このロールがロールスロットを持つかどうか
     *
     * @return ロールスロットを持つ場合 true
     */
    public boolean hasSubRoleSlots() {
        return !slotItemTags.isEmpty();
    }

    /**
     * IDからサブロールを取得
     *
     * @param id ロールID
     * @return 対応するSubRole
     */
    public static SubRole fromAttributeId(int id) {
        for (SubRole role : values()) {
            if (role.attributeId == id) {
                return role;
            }
        }
        return ERROR;
    }

    /**
     * 名前からサブロールを取得（大文字小文字を区別しない）
     *
     * @param name ロール名
     * @return 対応するSubRole
     */
    public static SubRole fromCommandName(String name) {
        for (SubRole role : values()) {
            if (role.commandName.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return ERROR;
    }
}
