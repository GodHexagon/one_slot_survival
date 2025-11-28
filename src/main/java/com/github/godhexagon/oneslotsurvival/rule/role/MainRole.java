package com.github.godhexagon.oneslotsurvival.rule.role;

import com.github.godhexagon.oneslotsurvival.object.item.ModTags;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    MINER(1, "miner", List.of(ItemTags.PICKAXES, ModTags.Items.MINER_DIGGING_TOOLS, ModTags.Items.BUCKETS)),

    /**
     * 戦闘特化ロール
     */
    WARRIOR(2, "warrior", List.of(ModTags.Items.WARRIOR_MELEE_WEAPONS, ModTags.Items.WARRIOR_DEFENSIVE, ItemTags.BEDS)),

    /**
     * サバイバル特化ロール
     */
    SURVIVOR(3, "survivor", List.of(ItemTags.AXES, ModTags.Items.SURVIVOR_UTILITY_TOOLS, ItemTags.BUNDLES)),

    /**
     * 建築特化ロール
     */
    BUILDER(4, "builder", List.of(ModTags.Items.BUILDER_SOFT_BLOCKS, ModTags.Items.BUILDER_HARD_BLOCKS, ModTags.Items.BUILDER_WOOD_BLOCKS));

    private final int atttributeId;
    private final String commandName;
    private final List<TagKey<Item>> slotItemTags;
    private final RoleSlotHelper helper;

    MainRole(int attribute_id, String command_name, List<TagKey<Item>> slot_item_tags) {
        this.atttributeId = attribute_id;
        this.commandName = command_name;
        this.slotItemTags = slot_item_tags;
        
        // ヘルパーに独自のデータを格納
        List<RoleSlot> roleSlots = new ArrayList<>();
        // インデックス、アンロックされる順番、slot_item_tags引数の順番がすべて一致するようにする。
        int index = 0;
        // メインロールのスロットは2レベルに１回解放される。
        int levelUpTimes = 1;
        for (TagKey<Item> itemTag: slot_item_tags) {
            roleSlots.add(new RoleSlot(itemTag, index, levelUpTimes));
            index++;
            levelUpTimes += 2;
        }
        this.helper = new RoleSlotHelper(roleSlots);
    }

    @Override
    public int getAttributeId() {
        return atttributeId;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("oneslotsurvival.role." + commandName);
    }

    @Deprecated
    public List<TagKey<Item>> getSlotItemTags() {
        return slotItemTags;
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

    @Override
    public Map<Integer, List<RoleSlot>> getJustUnlockedRoleSlotsMap() {
        return helper.getJustUnlockedRoleSlotsMap();
    }

    @Override
    public List<RoleSlot> getJustUnlockedRoleSlots(int levelUpTimesInRole) {
        return helper.getJustUnlockedRoleSlots(levelUpTimesInRole);
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
