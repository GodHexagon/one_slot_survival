package com.github.godhexagon.oneslotsurvival.world.command.general;

import com.github.godhexagon.oneslotsurvival.cui.MessageSender;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ShowNext {
    public static LiteralArgumentBuilder<CommandSourceStack> build() {
        return Commands.literal("next")
            .executes(ShowNext::showChangeInfo);
    }

    private static int showChangeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // プレイヤーが発行した場合のみ動作
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be used by players"));
            return 0;
        }
        
        MessageSender.displayNextReward(player);

        return 1;
    }
}
