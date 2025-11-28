package com.github.godhexagon.oneslotsurvival.world.command.general;

import java.util.List;
import java.util.Map;

import com.github.godhexagon.oneslotsurvival.cui.CuiObjects;
import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleSlot;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleSlotProvider;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;
import com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ShowGameData {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("data")
            .executes(ShowGameData::showList)
            .then(Commands.literal("rolelist")
                .executes(ShowGameData::showList))
            .then(Commands.argument("roleName", StringArgumentType.word())
                .suggests(CommandUtils.ROLE_SUGGESTIONS)
                .executes(ShowGameData::showRole)
                .then(Commands.literal("slot")
                    .then(Commands.argument("slotNumber", IntegerArgumentType.integer(1, 3))
                        .executes(ShowGameData::showSlot))));
    }

    private static int showList(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            // ヘッダー
            player.sendSystemMessage(Component.literal(""));
            player.sendSystemMessage(
                Component.literal("Available Roles")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
            );
            player.sendSystemMessage(Component.literal(""));

            // 二つのリスト
            player.sendSystemMessage(
                Component.literal("Main Roles")
                    .withStyle(ChatFormatting.YELLOW)
            );
            for (Component line: CuiObjects.createRoleList(false, false)) {
                player.sendSystemMessage(Component.literal("  ").append(line));
            }
            player.sendSystemMessage(Component.literal(""));
            
            player.sendSystemMessage(
                Component.literal("Sub Roles")
                    .withStyle(ChatFormatting.YELLOW)
            );
            for (Component line: CuiObjects.createRoleList(true, false)) {
                player.sendSystemMessage(Component.literal("  ").append(line));
            }
            player.sendSystemMessage(Component.literal(""));

            // フッター
            player.sendSystemMessage(
                Component.literal("Click on a role name to see details")
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
            );
            player.sendSystemMessage(Component.literal(""));

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int showRole(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            String roleName = StringArgumentType.getString(context, "roleName");

            // メインロールとサブロールの両方から検索
            Role role = null;
            MainRole foundMainRole = MainRole.fromCommandName(roleName);
            if (foundMainRole != MainRole.ERROR && foundMainRole != MainRole.UNASSIGNED) {
                role = foundMainRole;
            } else {
                SubRole foundSubRole = SubRole.fromCommandName(roleName);
                if (foundSubRole != SubRole.ERROR && foundSubRole != SubRole.UNASSIGNED) {
                    role = foundSubRole;
                }
            }

            // 表示
            displayRoleDetail(player, role);

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
    
    private static void displayRoleDetail(ServerPlayer player, Role role) {
        // ヘッダー
        player.sendSystemMessage(
            Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(ChatFormatting.GRAY)
        );
        player.sendSystemMessage(
            Component.literal("  ")
                .append(role.getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD))
                .append(Component.literal(" (")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(role.getCommandName()))
                    .append(Component.literal(") "))
                    .append(Component.literal(role instanceof MainRole? "Main Role" : "Sub Role")))
        );
        player.sendSystemMessage(Component.literal(""));

        // 報酬
        if (!(role instanceof RoleSlotProvider roleSlotProvider)) {
            player.sendSystemMessage(
                Component.literal("This role has no level up rewards.")
                    .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        Map<Integer, List<RoleSlot>> rewards = roleSlotProvider.getJustUnlockedRoleSlotsMap();
        if (rewards.isEmpty()) {
            player.sendSystemMessage(
                Component.literal("This role has no level up rewards.")
                    .withStyle(ChatFormatting.GRAY)
            );
            return;
        }

        // レベルアップ回数順にソートして表示
        rewards.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                int levelUpTimes = entry.getKey();
                List<RoleSlot> slots = entry.getValue();

                // レベルアップ回数のヘッダー
                player.sendSystemMessage(
                    Component.literal("  Level Up Times: ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(String.valueOf(levelUpTimes))
                            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                );

                // 各スロットを表示
                for (RoleSlot slot : slots) {
                    // アイテム基本情報
                    Component slotDescription = CuiObjects.createSlotDescriptionNoRoleName(slot, role);
                    // アイテム例
                    Component itemList = CuiObjects.createExampleItemList(slot, 5);
                    
                    player.sendSystemMessage(
                        Component.literal("    ● ")
                            .append(slotDescription)
                            .append(Component.literal(" Examples: ").withStyle(ChatFormatting.GRAY))
                            .append(itemList)
                    );
                }

                player.sendSystemMessage(Component.literal(""));
            });
        
        player.sendSystemMessage(
            Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                .withStyle(ChatFormatting.GRAY)
        );
    }

    private static int showSlot(CommandContext<CommandSourceStack> context) {
        try {
            CommandSourceStack source = context.getSource();

            // プレイヤーが発行した場合のみ動作
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                source.sendFailure(Component.literal("This command can only be used by players"));
                return 0;
            }

            String roleName = StringArgumentType.getString(context, "roleName");
            int slotIndex = IntegerArgumentType.getInteger(context, "slotNumber") - 1;

            // メインロールとサブロールの両方から検索
            Role role = null;
            MainRole foundMainRole = MainRole.fromCommandName(roleName);
            if (foundMainRole != MainRole.ERROR && foundMainRole != MainRole.UNASSIGNED) {
                role = foundMainRole;
            } else {
                SubRole foundSubRole = SubRole.fromCommandName(roleName);
                if (foundSubRole != SubRole.ERROR && foundSubRole != SubRole.UNASSIGNED) {
                    role = foundSubRole;
                }
            }
            
            // 説明の翻訳キー
            String descriptionKey = "oneslotsurvival.slot." + role.getCommandName() + "." + slotIndex + ".description";
            Component description = Component.translatable(descriptionKey);

            // ロールスロット
            if (!(role instanceof RoleSlotProvider roleSlotProvider)) {
                source.sendFailure(Component.literal("A non-existent slot was specified."));
                return 0;
            }
            RoleSlot roleSlot = roleSlotProvider.getRoleSlots().get(slotIndex);
            
            player.sendSystemMessage(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.GRAY)
            );

            // 表示
            player.sendSystemMessage(
                Component.literal("  ")
                    .append(CuiObjects.createSlotDetailHeader(slotIndex, role))
            );

            player.sendSystemMessage(Component.literal(""));

            player.sendSystemMessage(
                Component.literal("  Unlocked after leveling up ")
                    .withStyle(ChatFormatting.AQUA)
                    .append(Component.literal(String.valueOf(roleSlot.levelUpTimesInRole()))
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD))
                    .append(" times.")
            );
            
            player.sendSystemMessage(Component.literal(""));

            player.sendSystemMessage(
                Component.literal("  ")
                    .append(description.copy())
                    .append(Component.literal(" Examples: "))
                    .append(CuiObjects.createExampleItemList(roleSlot, 20))
            );
            
            player.sendSystemMessage(
                Component.literal("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
                    .withStyle(ChatFormatting.GRAY)
            );

            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
