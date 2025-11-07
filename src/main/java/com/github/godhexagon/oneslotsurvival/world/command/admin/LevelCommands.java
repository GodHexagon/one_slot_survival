package com.github.godhexagon.oneslotsurvival.world.command.admin;

import com.github.godhexagon.oneslotsurvival.rule.attribute.LevelAttribute;
import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.github.godhexagon.oneslotsurvival.rule.level.Level;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils.getDefaultPlayers;

import java.util.Collection;

/**
 * レベル管理コマンド
 * /oneslot admin level サブコマンド群
 */
public class LevelCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("level")
            .then(Commands.literal("get")
                .executes(context -> getLevelStatus(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> getLevelStatus(context, EntityArgument.getPlayers(context, "players")))))
            .then(Commands.literal("set")
                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                    .executes(context -> setLevel(context, getDefaultPlayers(context), true))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> setLevel(context, EntityArgument.getPlayers(context, "players"), true)))))
            .then(Commands.literal("setNoClear")
                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                    .executes(context -> setLevel(context, getDefaultPlayers(context), false))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> setLevel(context, EntityArgument.getPlayers(context, "players"), false)))))
            .then(Commands.literal("clear")
                .executes(context -> clearLevel(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> clearLevel(context, EntityArgument.getPlayers(context, "players")))));
    }

    private static int getLevelStatus(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                int level = Level.getMain(player);
                context.getSource().sendSuccess(
                    () -> Component.literal("Level for " + player.getName().getString() + ": " + level),
                    false
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setLevel(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean resetExp) {
        try {
            int level = IntegerArgumentType.getInteger(context, "level");

            for (ServerPlayer player : players) {
                LevelAttribute.setLevel(player, level);
                if (resetExp) {
                    Exp.clear(player);
                }

                context.getSource().sendSuccess(
                    () -> Component.literal("Set level to " + level + " for " + player.getName().getString() +
                        (resetExp ? " (exp reset)" : " (exp preserved)")),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your level has been set to " + level +
                        (resetExp ? " (exp reset)" : " (exp preserved)") +
                        " by " + context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearLevel(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                Level.reset(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared level for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your level has been cleared by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }
}
