package com.github.godhexagon.oneslotsurvival.world.command.admin;

import com.github.godhexagon.oneslotsurvival.rule.role.MainRole;
import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleManager;
import com.github.godhexagon.oneslotsurvival.rule.role.SubRole;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerProgress;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.github.godhexagon.oneslotsurvival.world.command.util.CommandUtils.getDefaultPlayers;

import java.util.Collection;

/**
 * ロール管理コマンド
 * /oneslot admin role サブコマンド群
 */
public class RoleCommands {

    /**
     * ロール名の補完候補を提供するサジェスチョンプロバイダー
     */
    private static final SuggestionProvider<CommandSourceStack> ROLE_SUGGESTIONS = (context, builder) -> {
        // ERRORとUNASSIGNEDを除く全てのメインロールのコマンド名を補完候補として提供
        for (MainRole role : MainRole.values()) {
            if (role != MainRole.ERROR) {
                builder.suggest(role.getCommandName());
            }
        }
        // ERRORとUNASSIGNEDを除く全てのサブロールのコマンド名を補完候補として提供
        for (SubRole role : SubRole.values()) {
            if (role != SubRole.ERROR) {
                builder.suggest(role.getCommandName());
            }
        }
        return builder.buildFuture();
    };

    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("role")
            .then(Commands.literal("get")
                .executes(context -> getRoleStatus(context, getDefaultPlayers(context)))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(context -> getRoleStatus(context, EntityArgument.getPlayers(context, "players")))))
            .then(Commands.literal("set")
                .then(Commands.argument("roleName", StringArgumentType.word())
                    .suggests(ROLE_SUGGESTIONS)
                    .executes(context -> setRole(context, getDefaultPlayers(context), true))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> setRole(context, EntityArgument.getPlayers(context, "players"), true)))))
            .then(Commands.literal("setNoClear")
                .then(Commands.argument("roleName", StringArgumentType.word())
                    .suggests(ROLE_SUGGESTIONS)
                    .executes(context -> setRole(context, getDefaultPlayers(context), false))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> setRole(context, EntityArgument.getPlayers(context, "players"), false)))))
            .then(Commands.literal("clear")
                .executes(context -> clearRole(context, getDefaultPlayers(context)))
                .then(Commands.literal("main")
                    .executes(context -> clearMainRole(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> clearMainRole(context, EntityArgument.getPlayers(context, "players")))))
                .then(Commands.literal("sub")
                    .executes(context -> clearSubRole(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> clearSubRole(context, EntityArgument.getPlayers(context, "players")))))
                .then(Commands.literal("both")
                    .executes(context -> clearRole(context, getDefaultPlayers(context)))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(context -> clearRole(context, EntityArgument.getPlayers(context, "players"))))));
    }

    private static int getRoleStatus(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                MainRole mainRole = RoleManager.getMainRole(player);
                String mainRoleCommandName = mainRole.getCommandName();
                Component mainRoleDisplayName = mainRole.getDisplayName();

                context.getSource().sendSuccess(
                    () -> Component.literal("Main Role for " + player.getName().getString() + ": " + mainRoleCommandName + " (")
                        .append(mainRoleDisplayName)
                        .append(")"),
                    false
                );
                
                SubRole subRole = RoleManager.getSubRole(player);
                String subRoleCommandName = subRole.getCommandName();
                Component subRoleDisplayName = subRole.getDisplayName();

                context.getSource().sendSuccess(
                    () -> Component.literal("Sub Role for " + player.getName().getString() + ": " + subRoleCommandName + " (")
                        .append(subRoleDisplayName)
                        .append(")"),
                    false
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int setRole(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean resetProgress) {
        try {
            String roleName = StringArgumentType.getString(context, "roleName");

            // メインロールとサブロールの両方から検索
            Role role = null;
            MainRole foundMainRole = MainRole.fromCommandName(roleName);
            if (foundMainRole != MainRole.ERROR) {
                role = foundMainRole;
            } else {
                SubRole foundSubRole = SubRole.fromCommandName(roleName);
                if (foundSubRole != SubRole.ERROR) {
                    role = foundSubRole;
                }
            }

            if (role == null) {
                context.getSource().sendFailure(
                    Component.literal("Invalid role name: " + roleName + ".")
                );
                return 0;
            }

            for (ServerPlayer player : players) {
                // setNoClearでないとき
                if (resetProgress) {
                    PlayerProgress.changeRole(player, role);
                } else {
                    if (role instanceof MainRole mainRole) {
                        RoleManager.setMainRole(player, mainRole);
                    } else if (role instanceof SubRole subRole) {
                        RoleManager.setSubRole(player, subRole);
                    }
                }

                String roleCommandName = role.getCommandName();
                Component roleDisplayName = role.getDisplayName();

                context.getSource().sendSuccess(
                    () -> Component.literal("Set role to " + roleCommandName + " (")
                        .append(roleDisplayName)
                        .append(") for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your role has been set to " + roleCommandName + " (")
                        .append(roleDisplayName)
                        .append(") by " + context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearRole(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                RoleManager.clearRole(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared all role for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your all role has been cleared by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearMainRole(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                RoleManager.clearMainRole(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared main role for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your main role has been cleared by " +
                        context.getSource().getDisplayName().getString())
                );
            }

            return players.size();
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error: " + e.getMessage()));
            return 0;
        }
    }

    private static int clearSubRole(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) {
        try {
            for (ServerPlayer player : players) {
                RoleManager.clearSubRole(player);

                context.getSource().sendSuccess(
                    () -> Component.literal("Cleared sub role for " + player.getName().getString()),
                    true
                );

                // 対象プレイヤーに通知
                player.sendSystemMessage(
                    Component.literal("Your sub role has been cleared by " +
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
