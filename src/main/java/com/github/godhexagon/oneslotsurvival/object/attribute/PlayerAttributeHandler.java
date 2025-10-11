package com.github.godhexagon.oneslotsurvival.object.attribute;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;

/**
 * Event handler for adding custom attributes to players.
 */
public class PlayerAttributeHandler {

    /**
     * Add our custom attributes to the player entity.
     * This event is fired on the mod event bus during startup.
     *
     * @param event the entity attribute modification event
     * @throws IllegalStateException if the MOD_ENABLED attribute is not registered
     */
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(
            EntityType.PLAYER,
            ModAttributes.MOD_ENABLED.getHolder().orElseThrow(
                () -> new IllegalStateException(
                    "MOD_ENABLED attribute is not registered. " +
                    "This indicates an issue with DeferredRegister initialization."
                )
            )
        );
    }
}
