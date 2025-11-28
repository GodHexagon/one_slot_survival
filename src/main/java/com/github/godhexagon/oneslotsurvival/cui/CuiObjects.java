package com.github.godhexagon.oneslotsurvival.cui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import com.github.godhexagon.oneslotsurvival.object.item.DynamicBuilderTags;
import com.github.godhexagon.oneslotsurvival.object.item.ModTags;
import com.github.godhexagon.oneslotsurvival.rule.inventory.SlotRestriction;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleSlot;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class CuiObjects {

    /**
     * ロールスロットのツールチップを生成
     *
     * @param inventoryIndex インベントリインデックス
     * @param player プレイヤー
     * @return ツールチップの行リスト
     */
    public static List<Component> createRoleSlotTooltip(int inventoryIndex, Player player) {
        List<Component> tooltip = new ArrayList<>();

        int roleSlotIndex = -1;
        Role role;
        if (SlotRestriction.unlockedMainRoleSlot(inventoryIndex, player)) {
            roleSlotIndex = SlotRestriction.getMainRoleSlotIndex(inventoryIndex);
            role = RoleManager.getMainRole(player);
        } else {
            roleSlotIndex = SlotRestriction.getSubRoleSlotIndex(inventoryIndex, player);
            role = RoleManager.getSubRole(player);
        }

        // スロット名の翻訳キー
        String slotNameKey = "oneslotsurvival.slot." + role.getCommandName() + "." + roleSlotIndex + ".name";

        // スロット名を追加
        tooltip.add(Component.translatable(slotNameKey).withStyle(ChatFormatting.GOLD));

        // ロール名と説明の翻訳キーから本文を生成
        String roleNameKey = "oneslotsurvival.role." + role.getCommandName();
        Component roleName = Component.translatable(roleNameKey);
        String descriptionKey = "oneslotsurvival.slot." + role.getCommandName() + "." + roleSlotIndex + ".description";
        Component description = Component.translatable(descriptionKey);

        String descriptionText = "[" + roleName.getString() + "] " + description.getString();

        // 説明文を取得して単語単位で分割
        List<String> wrappedLines = wrapText(descriptionText, 20); // 1行あたり約20文字で改行

        // 分割された各行をツールチップに追加
        for (String line : wrappedLines) {
            tooltip.add(Component.literal(line).withStyle(ChatFormatting.GRAY));
        }

        return tooltip;
    }

    /**
     * テキストを指定した文字数で単語単位で改行する
     *
     * @param text 改行対象のテキスト
     * @param maxLength 1行あたりの最大文字数
     * @return 改行されたテキストのリスト
     */
    private static List<String> wrapText(String text, int maxLength) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // 現在の行に単語を追加すると最大文字数を超える場合
            if (currentLine.length() > 0 && currentLine.length() + 1 + word.length() > maxLength) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
            }

            // 現在の行に単語を追加
            if (currentLine.length() > 0) {
                currentLine.append(" ");
            }
            currentLine.append(word);
        }

        // 最後の行を追加
        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * ロールスロットの情報を簡潔に表示する
     * 
     * @param roleSlot ロールスロット
     * @param role ロール（名前を取得するための）
     * @return ロールスロットの情報が読み取れるメッセージ
     */
    public static Component createSlotDescription(RoleSlot roleSlot, Role role) {
        int slotIndex = roleSlot.index();

        // スロット名の翻訳キー
        String slotNameKey = "oneslotsurvival.slot." + role.getCommandName() + "." + slotIndex + ".name";
        Component slotName = Component.translatable(slotNameKey);

        // ロール名の翻訳キー
        String roleNameKey = "oneslotsurvival.role." + role.getCommandName();
        Component roleName = Component.translatable(roleNameKey);

        // 説明の翻訳キー
        String descriptionKey = "oneslotsurvival.slot." + role.getCommandName() + "." + slotIndex + ".description";
        Component description = Component.translatable(descriptionKey);

        // フォーマット: "Slot <slot name> [<role name>] <description>"
        return Component.literal("")
            .append(CuiUtil.addSlotCommandSugguestion(slotName.copy().withStyle(ChatFormatting.GOLD), role, roleSlot))
            .append(Component.literal(" [").withStyle(ChatFormatting.GRAY))
            .append(CuiUtil.addRoleCommandSugguestion(roleName.copy().withStyle(ChatFormatting.LIGHT_PURPLE), role))
            .append(Component.literal("] ").withStyle(ChatFormatting.GRAY))
            .append(description.copy().withStyle(ChatFormatting.GRAY));
    }
    
    /**
     * ロールスロットの情報を簡潔に表示する
     * 
     * @param roleSlot ロールスロット
     * @param role ロール（名前を取得するための）
     * @return ロールスロットの情報が読み取れるメッセージ
     */
    public static Component createSlotDescriptionNoRoleName(RoleSlot roleSlot, Role role) {
        int slotIndex = roleSlot.index();

        // スロット名の翻訳キー
        String slotNameKey = "oneslotsurvival.slot." + role.getCommandName() + "." + slotIndex + ".name";
        Component slotName = Component.translatable(slotNameKey);

        // 説明の翻訳キー
        String descriptionKey = "oneslotsurvival.slot." + role.getCommandName() + "." + slotIndex + ".description";
        Component description = Component.translatable(descriptionKey);

        return Component.literal("")
            .append(CuiUtil.addSlotCommandSugguestion(slotName.copy().withStyle(ChatFormatting.GOLD), role, roleSlot))
            .append(Component.literal(" "))
            .append(description.copy().withStyle(ChatFormatting.GRAY));
    }
    
    /**
     * ロールスロットの情報を簡潔に表示する
     */
    public static Component createSlotDetailHeader(int slotIndex, Role role) {
        // スロット名の翻訳キー
        String slotNameKey = "oneslotsurvival.slot." + role.getCommandName() + "." + slotIndex + ".name";
        Component slotName = Component.translatable(slotNameKey);

        // ロール名の翻訳キー
        String roleNameKey = "oneslotsurvival.role." + role.getCommandName();
        Component roleName = Component.translatable(roleNameKey);

        return Component.literal("")
            .withStyle(ChatFormatting.GRAY)
            .append(slotName.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
            .append(Component.literal(" (" + getOrdinal(slotIndex + 1) + " slot of role "))
            .append(CuiUtil.addRoleCommandSugguestion(roleName.copy().withStyle(ChatFormatting.LIGHT_PURPLE), role))
            .append(Component.literal(") "));
    }

    /**
     * 数字を序数詞に変換（1→1st, 2→2nd, 3→3rd）
     *
     * @param number 数値
     * @return 序数詞形式の文字列
     */
    private static String getOrdinal(int number) {
        return switch (number) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> number + "th";
        };
    }

    public static Component createExampleItemList(RoleSlot slot, int itemCount) {
        // アイテム例を表示
        TagKey<Item> itemTag = slot.itemTag();
        List<Item> items = StreamSupport.stream(
            BuiltInRegistries.ITEM.getTagOrEmpty(itemTag).spliterator(),
            false
        )
        .map(Holder::value)
        .limit(itemCount) // 最大5個まで表示
        .toList();

        if (items.isEmpty() && DynamicBuilderTags.isInitialized()) {
            // Builderロールの3つのタグを確認
            if (itemTag.equals(ModTags.Items.BUILDER_SOFT_BLOCKS)) {
                items = List.copyOf(DynamicBuilderTags.getSoftItems()).subList(0, itemCount);
            } else if (itemTag.equals(ModTags.Items.BUILDER_HARD_BLOCKS)) {
                items = List.copyOf(DynamicBuilderTags.getHardItems()).subList(0, itemCount);
            } else if (itemTag.equals(ModTags.Items.BUILDER_WOOD_BLOCKS)) {
                items = List.copyOf(DynamicBuilderTags.getWoodItems()).subList(0, itemCount);
            }
        } 

        MutableComponent itemList = Component.literal("");
        if (!items.isEmpty()) {
            for (int i = 0; i < items.size(); i++) {
                Item item = items.get(i);
                ItemStack stack = new ItemStack(item);

                if (i > 0) {
                    itemList.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                }
                itemList.append(stack.getHoverName().copy().withStyle(ChatFormatting.GREEN));
            }
        }

        return itemList;
    }
    
    public static List<Component> createRoleList(boolean subRole, boolean suggestToChangeRole) {
        List<Component> list = new ArrayList<>();
        // すべてのロールを表示（ERROR と UNASSIGNED を除く）
        for (Role role : subRole? SubRole.values() : MainRole.values()) {
            if (role == MainRole.ERROR || role == MainRole.UNASSIGNED || role == SubRole.ERROR || role == SubRole.UNASSIGNED) {
                continue;
            }

            // ロール名
            Component roleDisplayName = role.getDisplayName();

            // クリック可能なリンク
            String detailCommand = suggestToChangeRole? "/oneslot changerole " + role.getCommandName() : "/oneslot data " + role.getCommandName();
            Component clickableRoleName = roleDisplayName.copy()
                .withStyle(style -> style
                    .withColor(ChatFormatting.LIGHT_PURPLE)
                    .withBold(true)
                    .withClickEvent(new ClickEvent.SuggestCommand(detailCommand))
                    .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW)))
                );

            list.add(
                Component.literal("• ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(clickableRoleName)
            );
        }

        return list;
    }
}
