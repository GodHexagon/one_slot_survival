package com.github.godhexagon.oneslotsurvival.server.player;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

/**
 * Event handler for adding custom attributes to players.
 */
public class PlayerAttributeHandler {

    /**
     * Add our custom attributes to the player entity.
     * This event is fired on the mod event bus during startup.
     */
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ModAttributes.MOD_ENABLED.getHolder().get());
    }
}
