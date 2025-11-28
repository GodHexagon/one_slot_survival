package com.github.godhexagon.oneslotsurvival.cui;

import com.github.godhexagon.oneslotsurvival.rule.role.Role;
import com.github.godhexagon.oneslotsurvival.rule.role.RoleSlot;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class CuiUtil {
    public static MutableComponent addRoleCommandSugguestion(MutableComponent message, Role role) {
        String commandLiteral = "/oneslot data " + role.getCommandName();
        return message.withStyle(style -> style
            .withClickEvent(new ClickEvent.SuggestCommand(commandLiteral))
            .withHoverEvent(new HoverEvent.ShowText(
                Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW))));
    }

    public static MutableComponent addSlotCommandSugguestion(MutableComponent message, Role role, RoleSlot slot) {
        String commandLiteral = "/oneslot data " + role.getCommandName() + " slot " + (slot.index() + 1);
        return message.withStyle(style -> style
            .withClickEvent(new ClickEvent.SuggestCommand(commandLiteral))
            .withHoverEvent(new HoverEvent.ShowText(
                Component.literal("Click to auto-fill command").withStyle(ChatFormatting.YELLOW))));
    }
}
