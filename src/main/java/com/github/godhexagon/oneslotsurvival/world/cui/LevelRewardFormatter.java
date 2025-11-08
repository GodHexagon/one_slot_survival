package com.github.godhexagon.oneslotsurvival.world.cui;

import com.github.godhexagon.oneslotsurvival.rule.role.LevelReward;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.List;

/**
 * レベルアップ報酬の表示フォーマットを統一的に管理するクラス
 * <p>
 * RoleDetailCommandとPlayerProgressで同じフォーマットを使用するために作成。
 * </p>
 */
public class LevelRewardFormatter {

    /**
     * スロット解放報酬の完全な詳細表示
     * <p>
     * RoleDetailCommandで使用される形式。
     * レベル、スロット番号、説明、アイテム例を含む。
     * </p>
     *
     * @param player プレイヤー
     * @param role ロール
     * @param reward スロット解放報酬
     */
    public static void displayUnlockSlotRewardDetailed(ServerPlayer player, MainRole role, LevelReward.UnlockSlot reward) {
        int level = reward.levelUpTimesInRole();
        int slotNumber = reward.slotNumber();
        int slotIndex = reward.slotIndex();
        TagKey<Item> tag = reward.itemTag();

        String levelOrdinal = TextFormatUtils.getOrdinal(level);
        String slotOrdinal = TextFormatUtils.getOrdinal(slotNumber);

        // スロットの説明を翻訳キーから取得
        String descriptionKey = "slot.oneslotsurvival." + role.getCommandName() + "." + slotIndex + ".description";

        // レベルアップメッセージ
        Component levelUpMessage = Component.literal("- " + levelOrdinal + " level up: ")
            .withStyle(ChatFormatting.WHITE)
            .append(Component.literal("Unlock " + slotOrdinal + " slot ")
                .withStyle(ChatFormatting.GREEN)
                .withStyle(style -> style
                    .withClickEvent(new ClickEvent.SuggestCommand("/oneslot slot detail " + slotNumber))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to view slot details")))))
            .append(Component.translatable(descriptionKey)
                .withStyle(ChatFormatting.GRAY));

        player.sendSystemMessage(levelUpMessage);

        // アイテム例の取得と表示
        displayExampleItems(player, tag);
    }

    /**
     * スロット解放報酬のレベルアップ時通知表示
     * <p>
     * PlayerProgressで使用される形式。
     * レベルアップ時のReward通知として表示される。
     * RoleDetailCommandと同じフォーマットを使用。
     * </p>
     *
     * @param player プレイヤー
     * @param role ロール
     * @param reward スロット解放報酬
     */
    public static void displayUnlockSlotRewardNotification(ServerPlayer player, MainRole role, LevelReward.UnlockSlot reward) {
        int slotNumber = reward.slotNumber();
        int slotIndex = reward.slotIndex();
        TagKey<Item> tag = reward.itemTag();

        String slotOrdinal = TextFormatUtils.getOrdinal(slotNumber);

        // スロットの説明を翻訳キーから取得
        String descriptionKey = "slot.oneslotsurvival." + role.getCommandName() + "." + slotIndex + ".description";

        // 報酬メッセージ
        Component rewardMessage = Component.literal("Reward: ")
            .withStyle(ChatFormatting.YELLOW)
            .append(Component.literal("Unlock " + slotOrdinal + " slot ")
                .withStyle(ChatFormatting.GREEN)
                .withStyle(style -> style
                    .withClickEvent(new ClickEvent.SuggestCommand("/oneslot slot detail " + slotNumber))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to view slot details")))))
            .append(Component.translatable(descriptionKey)
                .withStyle(ChatFormatting.GRAY));

        player.sendSystemMessage(rewardMessage);

        // アイテム例の取得と表示
        displayExampleItems(player, tag);
    }

    /**
     * アイテムタグの例示を表示
     * <p>
     * いくつかのアイテム例と、残りのアイテム数を表示。
     * </p>
     *
     * @param player プレイヤー
     * @param tag アイテムタグ
     * @param itemCount 表示されるアイテム例の数
     */
    private static void displayExampleItems(ServerPlayer player, TagKey<Item> tag) {
        List<Component> exampleItems = ItemDisplayUtils.getExampleItems(tag, 3);
        if (!exampleItems.isEmpty()) {
            MutableComponent exampleMessage = Component.literal("  Example: ")
                .withStyle(ChatFormatting.GRAY);

            for (int j = 0; j < exampleItems.size(); j++) {
                exampleMessage = exampleMessage.append(
                    Component.literal("'")
                        .withStyle(ChatFormatting.GRAY)
                        .append(exampleItems.get(j).copy().withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("'").withStyle(ChatFormatting.GRAY))
                );

                if (j < exampleItems.size() - 1) {
                    exampleMessage = exampleMessage.append(Component.literal(", ").withStyle(ChatFormatting.GRAY));
                }
            }

            // 残りのアイテム数を計算して表示
            int totalItems = ItemDisplayUtils.getTotalItemsInTag(tag);
            int remainingItems = totalItems - exampleItems.size();
            if (remainingItems > 0) {
                exampleMessage = exampleMessage.append(
                    Component.literal(", ...more " + remainingItems + " items")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
                );
            }
            exampleMessage = exampleMessage.append(Component.literal(".").withStyle(ChatFormatting.GRAY));

            player.sendSystemMessage(exampleMessage);
        }
    }

    /**
     * スロット詳細を表示する（SlotDetailCommand用）
     * <p>
     * スロット番号、ロール、説明、アイテム例を表示。
     * </p>
     *
     * @param player プレイヤー
     * @param slotNumber スロット番号（1-based）
     * @param role ロール
     * @param tag アイテムタグ（空の場合はアイテム例を表示しない）
     */
    public static void displaySlotDetail(ServerPlayer player, int slotNumber, MainRole role, TagKey<Item> tag, int itemCount) {
        int slotIndex = slotNumber - 1; // 1-based → 0-based

        // スロット番号の序数詞
        String slotOrdinal = TextFormatUtils.getOrdinal(slotNumber);

        // ロール表示名
        Component roleDisplayName = role.getDisplayName();

        // スロットの説明を翻訳キーから取得
        String descriptionKey = "slot.oneslotsurvival." + role.getCommandName() + "." + slotIndex + ".description";

        // ヘッダー
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(
            Component.literal(slotOrdinal + " slot of role '")
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                .append(roleDisplayName.copy().withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .append(Component.literal("'").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
        );
        player.sendSystemMessage(Component.literal(""));

        // 説明
        player.sendSystemMessage(Component.translatable(descriptionKey).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.literal(""));

        // アイテム例
        if (tag != null) {
            displaySlotDetailExampleItems(player, tag, itemCount);
        }

        player.sendSystemMessage(Component.literal(""));
    }

    /**
     * スロット詳細用のアイテム例を表示
     * <p>
     * SlotDetailCommandの表示形式（YELLOWベース）で表示。
     * </p>
     *
     * @param player プレイヤー
     * @param tag アイテムタグ
     * @param itemCount 表示されるアイテム例の数
     */
    private static void displaySlotDetailExampleItems(ServerPlayer player, TagKey<Item> tag, int itemCount) {
        List<Component> exampleItems = ItemDisplayUtils.getExampleItems(tag, itemCount);
        if (!exampleItems.isEmpty()) {
            MutableComponent exampleMessage = Component.literal("Example: ")
                .withStyle(ChatFormatting.YELLOW);

            for (int i = 0; i < exampleItems.size(); i++) {
                exampleMessage = exampleMessage.append(
                    Component.literal("'")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(exampleItems.get(i).copy().withStyle(ChatFormatting.WHITE))
                        .append(Component.literal("'").withStyle(ChatFormatting.YELLOW))
                );

                if (i < exampleItems.size() - 1) {
                    exampleMessage = exampleMessage.append(Component.literal(", ").withStyle(ChatFormatting.YELLOW));
                }
            }

            // 残りのアイテム数を計算して表示
            int totalItems = ItemDisplayUtils.getTotalItemsInTag(tag);
            int remainingItems = totalItems - exampleItems.size();
            if (remainingItems > 0) {
                exampleMessage = exampleMessage.append(
                    Component.literal(", ...more " + remainingItems + " items")
                        .withStyle(ChatFormatting.GRAY)
                );
            }
            exampleMessage = exampleMessage.append(Component.literal(".").withStyle(ChatFormatting.YELLOW));

            player.sendSystemMessage(exampleMessage);
        }
    }
}
