package com.github.godhexagon.oneslotsurvival.world.command.admin;

import com.github.godhexagon.oneslotsurvival.rule.level.Exp;
import com.mojang.brigadier.arguments.DoubleArgumentType;
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
 * 経験値管理コマンド
 * /oneslot admin exp サブコマンド群
 */
public class XpCommands {

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("xp")
            .then(Commands.literal("getRemaining")
                .executes(context -> getExpStatus(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> getExpStatus(context, EntityArgument.getPlayers(context, "players")))))
            .then(Commands.literal("add")
                .then(Commands.argument("amount", DoubleArgumentType.doubleArg(0.0))
                    .executes(context -> addExp(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> addExp(context, EntityArgument.getPlayers(context, "players"))))))
            .then(Commands.literal("clear")
                .executes(context -> clearExp(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> clearExp(context, EntityArgument.getPlayers(context, "players")))));
    }

    private static int getExpStatus(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                double remaining = Exp.get(player);
                context.getSource().sendSuccess(
                    () -> Component.literal("Remaining exp for " + player.getName().getString() + ": " + remaining),
                    false
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int addExp(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            double amount = DoubleArgumentType.getDouble(context, "amount");

            for (ServerPlayer player : players) {
                Exp.add(player, amount);

                context.getSource().sendSuccess(
                    () -> Component.literal("Added " + amount + " exp to " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("You received " + amount + " exp from " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearExp(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                Exp.clear(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared exp for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your exp has been cleared by " +
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
