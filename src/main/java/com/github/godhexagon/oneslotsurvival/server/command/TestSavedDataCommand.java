package com.github.godhexagon.oneslotsurvival.server.command;

import com.github.godhexagon.oneslotsurvival.world.data.TestSavedData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

/**
 * Test command to demonstrate SavedData usage
 * Commands:
 *   /testsaveddata increment - Increment counter
 *   /testsaveddata get - Get counter value
 *   /testsaveddata set <text> - Set data string
 */
public class TestSavedDataCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("testsaveddata")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("increment")
                    .executes(context -> {
                        ServerLevel level = context.getSource().getLevel();
                        TestSavedData data = TestSavedData.getForLevel(level);
                        data.incrementCounter();
                        context.getSource().sendSuccess(
                            () -> Component.literal("Counter incremented to: " + data.getCounter()),
                            true
                        );
                        return data.getCounter();
                    })
                )
                .then(Commands.literal("get")
                    .executes(context -> {
                        ServerLevel level = context.getSource().getLevel();
                        TestSavedData data = TestSavedData.getForLevel(level);
                        context.getSource().sendSuccess(
                            () -> Component.literal("Counter: " + data.getCounter() + ", Data: '" + data.getExampleData() + "'"),
                            false
                        );
                        return 1;
                    })
                )
                .then(Commands.literal("set")
                    .then(Commands.argument("text", StringArgumentType.string())
                        .executes(context -> {
                            String text = StringArgumentType.getString(context, "text");
                            ServerLevel level = context.getSource().getLevel();
                            TestSavedData data = TestSavedData.getForLevel(level);
                            data.setExampleData(text);
                            context.getSource().sendSuccess(
                                () -> Component.literal("Data set to: '" + text + "'"),
                                true
                            );
                            return 1;
                        })
                    )
                )
        );
    }
}
