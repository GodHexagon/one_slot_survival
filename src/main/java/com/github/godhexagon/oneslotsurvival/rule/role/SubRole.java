package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.network.chat.Component;
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
    UNASSIGNED(0, "sub_role_unassigned", Collections.emptyList());

    // TODO: 実際のサブロールを追加する
    // 例:
    // FARMER(1, "farmer", List.of(ItemTags.HOES, ModTags.Items.FARMING_TOOLS)),
    // BUILDER(2, "builder", List.of(ItemTags.AXES, ModTags.Items.BUILDING_TOOLS));

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
     * </p>
     *
     * @return アイテムタグのリスト（スロット順）
     */
    @Override
    public List<TagKey<Item>> getSlotItemTags() {
        return slotItemTags;
    }

    /**
     * このロールがロールスロットを持つかどうか
     *
     * @return ロールスロットを持つ場合 true
     */
    @Override
    public boolean hasRoleSlots() {
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
