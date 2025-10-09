package com.github.godhexagon.oneslotsurvival.server.command;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Event handler for One Slot Survival mod.
 * Handles command registration and other game events.
 */
@Mod.EventBusSubscriber(modid = OneSlotSurvivalMod.MODID)
public class CommandEvent {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // イベント発火タイミングの検証
        String threadName = Thread.currentThread().getName();
        Commands.CommandSelection env = event.getCommandSelection();

        LOGGER.info("=== RegisterCommandsEvent fired ===");
        LOGGER.info("Thread: {}", threadName);
        LOGGER.info("Environment: {}", env);
        LOGGER.info("===================================");

        // 既存のコマンド登録
        CommandRegisterer.register(event.getDispatcher());

        // テストコマンドの登録
        registerTestCommand(event);
    }

    /**
     * コマンド実行環境を検証するためのテストコマンドを登録
     */
    @SubscribeEvent
    private static void registerTestCommand(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("testside")
                .requires(source -> true) // 誰でも実行可能（権限不要）
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    String threadName = Thread.currentThread().getName();
                    boolean isClientSide = source.getLevel().isClientSide();
                    String playerName = source.getEntity() != null ?
                        source.getEntity().getName().getString() : "N/A";

                    LOGGER.info("=== Command Execution ===");
                    LOGGER.info("Thread: {}", threadName);
                    LOGGER.info("isClientSide: {}", isClientSide);
                    LOGGER.info("Player: {}", playerName);
                    LOGGER.info("=========================");

                    source.sendSuccess(() -> Component.literal(
                        "Executed on: " + (isClientSide ? "CLIENT" : "SERVER") +
                        "\nThread: " + threadName +
                        "\nPlayer: " + playerName
                    ), false);

                    return 1;
                })
        );
    }
}