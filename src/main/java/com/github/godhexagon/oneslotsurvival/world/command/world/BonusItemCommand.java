package com.github.godhexagon.oneslotsurvival.world.command.world;

import com.github.godhexagon.oneslotsurvival.object.storage.BonusItem;
import com.github.godhexagon.oneslotsurvival.object.storage.WorldOptions;
import com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

/**
 * ボーナスアイテム設定を管理するコマンドクラス。
 * <p>/osw bonusItem <none|bundle|shulkerbox|bundle_respawn|shulkerbox_respawn></p>
 * <p>/osw bonusItem (照会)</p>
 */
public class BonusItemCommand {

    /**
     * bonusItem コマンドを構築
     */
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("bonusItem")
            .then(Commands.argument("itemType", StringArgumentType.word())
                .suggests(CommandUtils.BONUS_ITEM_SUGGESTIONS)
                .executes(BonusItemCommand::setBonusItem))
            .executes(BonusItemCommand::queryBonusItem);
    }

    /**
     * ボーナスアイテム設定を変更する
     */
    private static int setBonusItem(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            String itemTypeName = StringArgumentType.getString(context, "itemType");
            BonusItem bonusItem = BonusItem.fromCommandName(itemTypeName);

            // WorldOptionsを使用して設定
            WorldOptions.setBonusItem(server, bonusItem);

            // 成功メッセージ
            source.sendSuccess(
                () -> Component.literal("Bonus item has been set to ")
                    .withStyle(ChatFormatting.GREEN)
                    .append(Component.literal(bonusItem.getDisplayName())
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)),
                true
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    /**
     * ボーナスアイテム設定を照会する
     */
    private static int queryBonusItem(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();
            MinecraftServer server = source.getServer();

            // WorldOptionsを使用して取得
            BonusItem currentItem = WorldOptions.getBonusItem(server);

            source.sendSuccess(
                () -> Component.literal("Bonus item is currently ")
                    .withStyle(ChatFormatting.YELLOW)
                    .append(Component.literal(currentItem.getDisplayName())
                        .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)),
                false
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
