package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.Set;

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
    @Nullable
    private MenuType<?> menuType;

    @Shadow
    private int quickcraftStatus;

    @Shadow
    private int quickcraftType;

    @Shadow
    @Final
    private Set<Slot> quickcraftSlots;

    @Shadow
    public abstract ItemStack getCarried();

    @Shadow
    public abstract void setCarried(ItemStack stack);

    @Shadow
    protected abstract void resetQuickCraft();

    @Shadow
    protected abstract boolean canDragTo(Slot slot);

    @Shadow
    public abstract ItemStack quickMoveStack(Player player, int index);

    @Shadow
    protected abstract boolean tryItemClickBehaviourOverride(Player player, ClickAction action, Slot slot, ItemStack clickedItem, ItemStack carriedItem);

    @Shadow
    protected abstract net.minecraft.world.entity.SlotAccess createCarriedSlotAccess();

    @Shadow
    protected abstract boolean canTakeItemForPickAll(ItemStack stack, Slot slot);

    @Shadow
    protected abstract void doClick(int slotId, int button, ClickType clickType, Player player);

    @Shadow
    public static int getQuickcraftHeader(int p_38980_) {
        throw new AssertionError();
    }

    @Shadow
    public static int getQuickcraftType(int p_38986_) {
        throw new AssertionError();
    }

    @Shadow
    public static boolean isValidQuickcraftType(int p_150421_, Player p_150422_) {
        throw new AssertionError();
    }

    @Shadow
    public static int getQuickCraftPlaceCount(Set<Slot> p_150426_, int p_150427_, ItemStack p_150428_) {
        throw new AssertionError();
    }

    @Shadow
    public static boolean canItemQuickReplace(@Nullable Slot p_150432_, ItemStack p_150433_, boolean p_150434_) {
        throw new AssertionError();
    }

    /**
     * Inject into clicked() to intercept ClickType.PICKUP on role slots.
     * This prevents non-pickaxe items from being placed in role slots.
     */
    @Inject(method = "clicked", at = @At("HEAD"), cancellable = true)
    private void onClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
        // 対象プレイヤーの時だけふるまいを変える
        if (PlayerModValidity.isEffective(player)) {
            ci.cancel();

            try {
                // ロールスロットかどうかは内部で判定
                this.RoledPlayerDoClick(slotId, button, clickType, player);
            } catch (Exception exception) {
                // エラーハンドリングは完全コピー
                CrashReport crashreport = CrashReport.forThrowable(exception, "Container click");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Click info");
                crashreportcategory.setDetail(
                        "Menu Type", () -> this.menuType != null ? BuiltInRegistries.MENU.getKey(this.menuType).toString() : "<no type>"
                );
                crashreportcategory.setDetail("Menu Class", () -> this.getClass().getCanonicalName());
                crashreportcategory.setDetail("Slot Count", this.slots.size());
                crashreportcategory.setDetail("Slot", slotId);
                crashreportcategory.setDetail("Button", button);
                crashreportcategory.setDetail("Type", clickType);
                throw new ReportedException(crashreport);
            }
        }
    }

    private void RoledPlayerDoClick(int p_150431_, int p_150432_, ClickType p_150433_, Player p_150434_) {
        // ここで独自実装をする。バグを減らすために、完全コピー状態にしてある。ここから変更を加える。
        Inventory inventory = p_150434_.getInventory();
        if (p_150433_ == ClickType.QUICK_CRAFT) {
            int i = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(p_150432_);
            if ((i != 1 || this.quickcraftStatus != 2) && i != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(p_150432_);
                if (isValidQuickcraftType(this.quickcraftType, p_150434_)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot slot = this.slots.get(p_150431_);
                ItemStack itemstack = this.getCarried();
                if (canItemQuickReplace(slot, itemstack, true)
                        && slot.mayPlace(itemstack)
                        && (this.quickcraftType == 2 || itemstack.getCount() > this.quickcraftSlots.size())
                        && this.canDragTo(slot)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int i1 = this.quickcraftSlots.iterator().next().index;
                        this.resetQuickCraft();
                        this.doClick(i1, this.quickcraftType, ClickType.PICKUP, p_150434_);
                        return;
                    }

                    ItemStack itemstack3 = this.getCarried().copy();
                    if (itemstack3.isEmpty()) {
                        this.resetQuickCraft();
                        return;
                    }

                    int k1 = this.getCarried().getCount();

                    for (Slot slot1 : this.quickcraftSlots) {
                        ItemStack itemstack1 = this.getCarried();
                        if (slot1 != null
                                && canItemQuickReplace(slot1, itemstack1, true)
                                && slot1.mayPlace(itemstack1)
                                && (this.quickcraftType == 2 || itemstack1.getCount() >= this.quickcraftSlots.size())
                                && this.canDragTo(slot1)) {
                            int j = slot1.hasItem() ? slot1.getItem().getCount() : 0;
                            int k = Math.min(itemstack3.getMaxStackSize(), slot1.getMaxStackSize(itemstack3));
                            int l = Math.min(getQuickCraftPlaceCount(this.quickcraftSlots, this.quickcraftType, itemstack3) + j, k);
                            k1 -= l - j;
                            slot1.setByPlayer(itemstack3.copyWithCount(l));
                        }
                    }

                    itemstack3.setCount(k1);
                    this.setCarried(itemstack3);
                }

                this.resetQuickCraft();
            } else {
                this.resetQuickCraft();
            }
        } else if (this.quickcraftStatus != 0) {
            this.resetQuickCraft();
        } else if ((p_150433_ == ClickType.PICKUP || p_150433_ == ClickType.QUICK_MOVE) && (p_150432_ == 0 || p_150432_ == 1)) {
            ClickAction clickaction = p_150432_ == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (p_150431_ == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (clickaction == ClickAction.PRIMARY) {
                        p_150434_.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    } else {
                        p_150434_.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (p_150433_ == ClickType.QUICK_MOVE) {
                if (p_150431_ < 0) {
                    return;
                }

                Slot slot6 = this.slots.get(p_150431_);
                if (!slot6.mayPickup(p_150434_)) {
                    return;
                }

                ItemStack itemstack8 = this.quickMoveStack(p_150434_, p_150431_);

                while (!itemstack8.isEmpty() && ItemStack.isSameItem(slot6.getItem(), itemstack8)) {
                    itemstack8 = this.quickMoveStack(p_150434_, p_150431_);
                }
            } else {
                if (p_150431_ < 0) {
                    return;
                }

                Slot slot7 = this.slots.get(p_150431_);
                ItemStack itemstack9 = slot7.getItem();
                ItemStack itemstack10 = this.getCarried();
                p_150434_.updateTutorialInventoryAction(itemstack10, slot7.getItem(), clickaction);
                if (!this.tryItemClickBehaviourOverride(p_150434_, clickaction, slot7, itemstack9, itemstack10)) {
                    if (!net.minecraftforge.event.ForgeEventFactory.onItemStackedOn(itemstack9, itemstack10, slot7, clickaction, p_150434_, createCarriedSlotAccess()))
                        if (itemstack9.isEmpty()) {
                            if (!itemstack10.isEmpty()) {
                                int i3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                                this.setCarried(slot7.safeInsert(itemstack10, i3));
                            }
                        } else if (slot7.mayPickup(p_150434_)) {
                            if (itemstack10.isEmpty()) {
                                int j3 = clickaction == ClickAction.PRIMARY ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
                                Optional<ItemStack> optional1 = slot7.tryRemove(j3, Integer.MAX_VALUE, p_150434_);
                                optional1.ifPresent(p_150421_ -> {
                                    this.setCarried(p_150421_);
                                    slot7.onTake(p_150434_, p_150421_);
                                });
                            } else if (slot7.mayPlace(itemstack10)) {
                                if (ItemStack.isSameItemSameComponents(itemstack9, itemstack10)) {
                                    int k3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                                    this.setCarried(slot7.safeInsert(itemstack10, k3));
                                } else if (itemstack10.getCount() <= slot7.getMaxStackSize(itemstack10)) {
                                    this.setCarried(itemstack9);
                                    slot7.setByPlayer(itemstack10);
                                }
                            } else if (ItemStack.isSameItemSameComponents(itemstack9, itemstack10)) {
                                Optional<ItemStack> optional = slot7.tryRemove(itemstack9.getCount(), itemstack10.getMaxStackSize() - itemstack10.getCount(), p_150434_);
                                optional.ifPresent(p_150428_ -> {
                                    itemstack10.grow(p_150428_.getCount());
                                    slot7.onTake(p_150434_, p_150428_);
                                });
                            }
                        }
                }

                slot7.setChanged();
            }
        } else if (p_150433_ == ClickType.SWAP && (p_150432_ >= 0 && p_150432_ < 9 || p_150432_ == 40)) {
            ItemStack itemstack2 = inventory.getItem(p_150432_);
            Slot slot5 = this.slots.get(p_150431_);
            ItemStack itemstack7 = slot5.getItem();
            if (!itemstack2.isEmpty() || !itemstack7.isEmpty()) {
                if (itemstack2.isEmpty()) {
                    if (slot5.mayPickup(p_150434_)) {
                        inventory.setItem(p_150432_, itemstack7);
                        // Use accessor to call protected onSwapCraft method
                        ((SlotAccessor) slot5).invokeOnSwapCraft(itemstack7.getCount());
                        slot5.setByPlayer(ItemStack.EMPTY);
                        slot5.onTake(p_150434_, itemstack7);
                    }
                } else if (itemstack7.isEmpty()) {
                    if (slot5.mayPlace(itemstack2)) {
                        int j2 = slot5.getMaxStackSize(itemstack2);
                        if (itemstack2.getCount() > j2) {
                            slot5.setByPlayer(itemstack2.split(j2));
                        } else {
                            inventory.setItem(p_150432_, ItemStack.EMPTY);
                            slot5.setByPlayer(itemstack2);
                        }
                    }
                } else if (slot5.mayPickup(p_150434_) && slot5.mayPlace(itemstack2)) {
                    int k2 = slot5.getMaxStackSize(itemstack2);
                    if (itemstack2.getCount() > k2) {
                        slot5.setByPlayer(itemstack2.split(k2));
                        slot5.onTake(p_150434_, itemstack7);
                        if (!inventory.add(itemstack7)) {
                            p_150434_.drop(itemstack7, true);
                        }
                    } else {
                        inventory.setItem(p_150432_, itemstack7);
                        slot5.setByPlayer(itemstack2);
                        slot5.onTake(p_150434_, itemstack7);
                    }
                }
            }
        } else if (p_150433_ == ClickType.CLONE && p_150434_.hasInfiniteMaterials() && this.getCarried().isEmpty() && p_150431_ >= 0) {
            Slot slot4 = this.slots.get(p_150431_);
            if (slot4.hasItem()) {
                ItemStack itemstack5 = slot4.getItem();
                this.setCarried(itemstack5.copyWithCount(itemstack5.getMaxStackSize()));
            }
        } else if (p_150433_ == ClickType.THROW && this.getCarried().isEmpty() && p_150431_ >= 0) {
            Slot slot3 = this.slots.get(p_150431_);
            int j1 = p_150432_ == 0 ? 1 : slot3.getItem().getCount();
            if (!p_150434_.canDropItems()) {
                return;
            }

            ItemStack itemstack6 = slot3.safeTake(j1, Integer.MAX_VALUE, p_150434_);
            p_150434_.drop(itemstack6, true);
            p_150434_.handleCreativeModeItemDrop(itemstack6);
            if (p_150432_ == 1) {
                while (!itemstack6.isEmpty() && ItemStack.isSameItem(slot3.getItem(), itemstack6)) {
                    if (!p_150434_.canDropItems()) {
                        return;
                    }

                    itemstack6 = slot3.safeTake(j1, Integer.MAX_VALUE, p_150434_);
                    p_150434_.drop(itemstack6, true);
                    p_150434_.handleCreativeModeItemDrop(itemstack6);
                }
            }
        } else if (p_150433_ == ClickType.PICKUP_ALL && p_150431_ >= 0) {
            Slot slot2 = this.slots.get(p_150431_);
            ItemStack itemstack4 = this.getCarried();
            if (!itemstack4.isEmpty() && (!slot2.hasItem() || !slot2.mayPickup(p_150434_))) {
                int l1 = p_150432_ == 0 ? 0 : this.slots.size() - 1;
                int i2 = p_150432_ == 0 ? 1 : -1;

                for (int l2 = 0; l2 < 2; l2++) {
                    for (int l3 = l1; l3 >= 0 && l3 < this.slots.size() && itemstack4.getCount() < itemstack4.getMaxStackSize(); l3 += i2) {
                        Slot slot8 = this.slots.get(l3);
                        if (slot8.hasItem() && canItemQuickReplace(slot8, itemstack4, true) && slot8.mayPickup(p_150434_) && this.canTakeItemForPickAll(itemstack4, slot8)) {
                            ItemStack itemstack11 = slot8.getItem();
                            if (l2 != 0 || itemstack11.getCount() != itemstack11.getMaxStackSize()) {
                                ItemStack itemstack12 = slot8.safeTake(itemstack11.getCount(), itemstack4.getMaxStackSize() - itemstack4.getCount(), p_150434_);
                                itemstack4.grow(itemstack12.getCount());
                            }
                        }
                    }
                }
            }
        }
    }
//    private void onClicked(int slotId, int button, ClickType clickType, Player player, CallbackInfo ci) {
//        LOGGER.info("=== onClicked DEBUG ===");
//        LOGGER.info("slotId: {}, button: {}, clickType: {}", slotId, button, clickType);
//        LOGGER.info("Player: {}, isEffective: {}", player.getName().getString(), PlayerModValidity.isEffective(player));
//        LOGGER.info("isRoleSlot({}): {}", slotId, SlotDefinition.isRoleSlot(slotId));
//
//        ItemStack carried = this.getCarried();
//        LOGGER.info("Carried item: {} (isEmpty: {})", carried, carried.isEmpty());
//
//        // Only process ClickType.PICKUP
//        if (clickType != ClickType.PICKUP) {
//            LOGGER.info("Not PICKUP, skipping");
//            return;
//        }
//
//        // Check if player is subject to mod restrictions
//        if (!PlayerModValidity.isEffective(player)) {
//            LOGGER.info("Player not effective, skipping");
//            return;
//        }
//
//        // Check if the slot is a role slot
//        if (!SlotDefinition.isRoleSlot(slotId)) {
//            LOGGER.info("Not a role slot, skipping");
//            return;
//        }
//
//        // Validate slot index
//        if (slotId < 0 || slotId >= this.slots.size()) {
//            LOGGER.warn("Invalid slot index: {}", slotId);
//            return;
//        }
//
//        Slot slot = this.slots.get(slotId);
//        ItemStack slotItem = slot.getItem();
//
//        LOGGER.info("Slot item: {} (isEmpty: {})", slotItem, slotItem.isEmpty());
//        LOGGER.info("isPickaxe(carried): {}", SlotDefinition.isPickaxe(carried));
//
//        // Scenario 1: Placing an item from cursor into role slot
//        if (!carried.isEmpty()) {
//            // Check if the carried item is a pickaxe
//            if (!SlotDefinition.isPickaxe(carried)) {
//                // Cancel the operation - don't allow non-pickaxe items
//                LOGGER.info("!!! BLOCKED non-pickaxe item placement in role slot {}: {}", slotId, carried);
//                ci.cancel();
//                return;
//            }
//        }
//
//        // Scenario 2: Swapping items between cursor and role slot
//        if (!carried.isEmpty() && !slotItem.isEmpty()) {
//            // The carried item is already checked above
//            // The item in the slot is already a pickaxe (or should be)
//            // We need to verify the carried item is a pickaxe
//            if (!SlotDefinition.isPickaxe(carried)) {
//                LOGGER.info("!!! BLOCKED non-pickaxe swap in role slot {}: {}", slotId, carried);
//                ci.cancel();
//                return;
//            }
//        }
//
//        LOGGER.info("Operation allowed");
//        // Allow the operation to proceed if:
//        // - Carried item is a pickaxe, or
//        // - Carried item is empty (picking up from slot)
//
//        /*
//        # 問題
//        - たまにシングルクリックも許可されることがある
//        - ~~作業台スロットがロールスロットになっている~~
//         */
//    }
}
