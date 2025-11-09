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
 * プレイヤーのメインロール定義
 * <p>
 * 各ロールはユニークなIDを持ち、プレイヤーのAttributeとして永続化される。
 * 各ロールは使用可能なアイテムタグのリストを持ち、ロールスロットの制限を定義する。
 * </p>
 */
public enum MainRole implements Role, RoleSlotProvider {
    /**
     * 不明な状態
     */
    ERROR(-1, "main_role_error", Collections.emptyList()),

    /**
     * ロール未割り当て状態
     */
    UNASSIGNED(0, "main_role_unassigned", Collections.emptyList()),

    /**
     * 採掘特化ロール
     */
    MINER(1, "miner", List.of(ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.AXES)),

    /**
     * 戦闘特化ロール
     */
    WARRIOR(2, "warrior", List.of(ItemTags.SWORDS, ModTags.Items.WARRIOR_DEFENSIVE, ModTags.Items.WARRIOR_SPECIAL_WEAPONS));

    private final int atttributeId;
    private final String commandName;
    private final List<TagKey<Item>> slotItemTags;

    MainRole(int attribute_id, String command_name, List<TagKey<Item>> slot_item_tags) {
        this.atttributeId = attribute_id;
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
        return atttributeId;
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
     * 例: index 0 = 1番目のロールスロット（つるはし等）
     * </p>
     *
     * @return アイテムタグのリスト（スロット順）
     */
    public List<TagKey<Item>> getSlotItemTags() {
        return slotItemTags;
    }

    @Override
    public List<RoleSlot> getRoleSlots() {
        List<RoleSlot> roleSlots = new ArrayList<>();
        // メインロールのスロットは2レベルに１回解放される。
        int levelUpTimes = 1;
        for (TagKey<Item> itemTag: slotItemTags) {
            roleSlots.add(new RoleSlot(itemTag, levelUpTimes));
            levelUpTimes += 2;
        }
        return roleSlots;
    }

    @Override
    public List<RoleSlot> getAvailableRoleSlots(int levelUpTimesInRole) {
        return getRoleSlots().stream()
            .filter(slot -> slot.levelUpTimesInRole() <= levelUpTimesInRole)
            .toList();
    }
    
    @Override
    public Optional<RoleSlot> getAvailableRoleSlot(int index, int levelUpTimesInRole) {
        if (-1 < index && index < getAvailableRoleSlots(levelUpTimesInRole).size()) {
            return Optional.of(getAvailableRoleSlots(levelUpTimesInRole).get(index));
        } else {
            return Optional.empty();
        }
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
     * IDからロールを取得
     *
     * @param id ロールID
     * @return 対応するMainRole
     */
    public static MainRole fromAttributeId(int id) {
        for (MainRole role : values()) {
            if (role.atttributeId == id) {
                return role;
            }
        }
        return ERROR;
    }

    /**
     * 名前からロールを取得（大文字小文字を区別しない）
     *
     * @param name ロール名
     * @return 対応するMainRole
     */
    public static MainRole fromCommandName(String name) {
        for (MainRole role : values()) {
            if (role.commandName.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return ERROR;
    }
}
