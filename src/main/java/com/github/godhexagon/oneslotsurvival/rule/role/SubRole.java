package com.github.godhexagon.oneslotsurvival.rule.role;

import com.github.godhexagon.oneslotsurvival.object.item.ModTags;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * プレイヤーのサブロール定義
 * <p>
 * サブロールはメインロールと並行して使用できる追加のロールです。
 * 各ロールはユニークなIDを持ち、プレイヤーのAttributeとして永続化される。
 * 各ロールは使用可能なアイテムタグのリストを持ち、ロールスロットの制限を定義する。
 * </p>
 */
public enum SubRole implements Role, RoleSlotProvider {
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
    private final RoleSlotHelper helper;

    SubRole(int attribute_id, String command_name, List<TagKey<Item>> slot_item_tags) {
        this.attributeId = attribute_id;
        this.commandName = command_name;
        
        // ヘルパーに独自のデータを格納
        List<RoleSlot> roleSlots = new ArrayList<>();
        for (TagKey<Item> itemTag: slot_item_tags) {
            // サブロールのスロットは第２回のレベルアップですべて一気に解放される。
            roleSlots.add(new RoleSlot(itemTag, 2));
        }
        this.helper = new RoleSlotHelper(roleSlots);
    }

    @Override
    public int getAttributeId() {
        return attributeId;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("oneslotsurvival.role." + commandName);
    }
    
    @Override
    public List<RoleSlot> getRoleSlots() {
        return helper.getRoleSlots();
    }

    @Override
    public List<RoleSlot> getAvailableRoleSlots(int levelUpTimesInRole) {
        return helper.getAvailableRoleSlots(levelUpTimesInRole);
    }
    
    @Override
    public Optional<RoleSlot> getAvailableRoleSlot(int index, int levelUpTimesInRole) {
        return helper.getAvailableRoleSlot(index, levelUpTimesInRole);
    }
    
    @Override
    public boolean hasRoleSlots() {
        return helper.hasRoleSlots();
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
