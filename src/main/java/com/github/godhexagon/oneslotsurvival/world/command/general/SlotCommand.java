package com.github.godhexagon.oneslotsurvival.world.command.general;

import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils;
import com.github.godhexagon.oneslotsurvival.world.cui.LevelRewardFormatter;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * スロット詳細表示コマンド（一般プレイヤー向け）
 * /oneslot slot detail &lt;Slot&gt; [role &lt;Role&gt;]
 */
public class SlotCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("slot")
            .then(Commands.literal("detail")
                .then(Commands.argument("slotNumber", IntegerArgumentType.integer(1, 3))
                    .executes(SlotCommand::showSlotDetailWithPlayerRole)
                    .then(Commands.literal("role")
                        .then(Commands.argument("roleName", StringArgumentType.word())
                            .suggests(CommandUtils.ROLE_SUGGESTIONS)
                            .executes(SlotCommand::showSlotDetailWithSpecifiedRole)))));
    }

    /**
     * プレイヤー自身のロールでスロット詳細を表示
     */
    private static int showSlotDetailWithPlayerRole(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            int slotNumber = IntegerArgumentType.getInteger(context, "slotNumber");
            MainRole playerRole = RoleManager.getRole(player);

            // ロールが未割り当てかチェック
            if (playerRole == MainRole.UNASSIGNED || playerRole == MainRole.ERROR) {
                player.sendSystemMessage(
                    Component.literal("You don't have a role yet. Please get a role first.")
                        .withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            return showSlotDetail(player, slotNumber, playerRole);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * 指定されたロールでスロット詳細を表示
     */
    private static int showSlotDetailWithSpecifiedRole(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            int slotNumber = IntegerArgumentType.getInteger(context, "slotNumber");
            String roleName = StringArgumentType.getString(context, "roleName");

            // ロール名の検証
            MainRole role = MainRole.fromCommandName(roleName);
            if (role == null || role == MainRole.ERROR || role == MainRole.UNASSIGNED) {
                player.sendSystemMessage(
                    Component.literal("Invalid role name: " + roleName + ".")
                        .withStyle(ChatFormatting.RED)
                );
                return 0;
            }

            return showSlotDetail(player, slotNumber, role);
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * スロット詳細を表示する共通処理
     */
    private static int showSlotDetail(ServerPlayer player, int slotNumber, MainRole role) {
        int roleSlotIndex = slotNumber - 1; // 1-based → 0-based

        // このスロットで使用可能なアイテムタグを取得（レベルアップ回数は無視して常に表示）
        TagKey<Item> tag = getSlotItemTag(role, roleSlotIndex);

        // LevelRewardFormatterに統一されたフォーマットで表示
        LevelRewardFormatter.displaySlotDetail(player, slotNumber, role, tag, 12);

        return 1;
    }

    /**
     * 指定されたロールとスロットインデックスのアイテムタグを取得
     * （レベルアップ回数によるロック状態は無視）
     */
    private static TagKey<Item> getSlotItemTag(MainRole role, int roleSlotIndex) {
        if (!role.hasRoleSlots()) {
            return null;
        }

        var tags = role.getSlotItemTags();
        if (roleSlotIndex < 0 || roleSlotIndex >= tags.size()) {
            return null;
        }

        return tags.get(roleSlotIndex);
    }
}
