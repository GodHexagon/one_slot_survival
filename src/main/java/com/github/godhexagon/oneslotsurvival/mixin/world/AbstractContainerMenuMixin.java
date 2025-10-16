package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import com.github.godhexagon.oneslotsurvival.world.util.SlotDefinition;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to restrict item placement in role slots.
 * Prevents non-pickaxe items from being placed in role slots (indices 1-3).
 */
@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("OneSlotSurvival/AbstractContainerMenuMixin");

    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Shadow
    public abstract ItemStack getCarried();

    @Shadow
    public abstract void setCarried(ItemStack stack);

    /**
     * Inject into clicked() to intercept ClickType.PICKUP on role slots.
     * This prevents non-pickaxe items from being placed in role slots.
     */
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void onClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        LOGGER.info("=== onClicked DEBUG ===");
        LOGGER.info("slotId: {}, button: {}, clickType: {}", slotId, button, clickType);
        LOGGER.info("Player: {}, isEffective: {}", player.getName().getString(), PlayerModValidity.isEffective(player));
        LOGGER.info("isRoleSlot({}): {}", slotId, SlotDefinition.isRoleSlot(slotId));

        ItemStack carried = this.getCarried();
        LOGGER.info("Carried item: {} (isEmpty: {})", carried, carried.isEmpty());

        // Only process ClickType.PICKUP
        if (clickType != ClickType.PICKUP) {
            LOGGER.info("Not PICKUP, skipping");
            return;
        }

        // Check if player is subject to mod restrictions
        if (!PlayerModValidity.isEffective(player)) {
            LOGGER.info("Player not effective, skipping");
            return;
        }

        // Check if the slot is a role slot
        if (!SlotDefinition.isRoleSlot(slotId)) {
            LOGGER.info("Not a role slot, skipping");
            return;
        }

        // Validate slot index
        if (slotId < 0 || slotId >= this.slots.size()) {
            LOGGER.warn("Invalid slot index: {}", slotId);
            return;
        }

        Slot slot = this.slots.get(slotId);
        ItemStack slotItem = slot.getItem();

        LOGGER.info("Slot item: {} (isEmpty: {})", slotItem, slotItem.isEmpty());
        LOGGER.info("isPickaxe(carried): {}", SlotDefinition.isPickaxe(carried));

        // Scenario 1: Placing an item from cursor into role slot
        if (!carried.isEmpty()) {
            // Check if the carried item is a pickaxe
            if (!SlotDefinition.isPickaxe(carried)) {
                // Cancel the operation - don't allow non-pickaxe items
                LOGGER.info("!!! BLOCKED non-pickaxe item placement in role slot {}: {}", slotId, carried);
                ci.cancel();
                return;
            }
        }

        // Scenario 2: Swapping items between cursor and role slot
        if (!carried.isEmpty() && !slotItem.isEmpty()) {
            // The carried item is already checked above
            // The item in the slot is already a pickaxe (or should be)
            // We need to verify the carried item is a pickaxe
            if (!SlotDefinition.isPickaxe(carried)) {
                LOGGER.info("!!! BLOCKED non-pickaxe swap in role slot {}: {}", slotId, carried);
                ci.cancel();
                return;
            }
        }

        LOGGER.info("Operation allowed");
        // Allow the operation to proceed if:
        // - Carried item is a pickaxe, or
        // - Carried item is empty (picking up from slot)
    }
}
