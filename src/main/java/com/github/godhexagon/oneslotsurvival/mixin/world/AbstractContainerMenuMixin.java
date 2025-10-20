package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.mixin.accessor.SlotAccessor;
import com.github.godhexagon.oneslotsurvival.world.util.ItemType;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
    @Shadow
    @Final
    public NonNullList<Slot> slots;

    @Final
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
    public abstract boolean canDragTo(Slot slot);

    @Shadow
    public abstract ItemStack quickMoveStack(Player player, int index);

    @Shadow
    protected abstract boolean tryItemClickBehaviourOverride(Player player, ClickAction action, Slot slot, ItemStack clickedItem, ItemStack carriedItem);

    @Shadow
    protected abstract SlotAccess createCarriedSlotAccess();

    @Shadow
    public abstract boolean canTakeItemForPickAll(ItemStack stack, Slot slot);

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
                this.one_slot_survival$roledPlayerDoClick(slotId, button, clickType, player);
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

    @Unique
    private void one_slot_survival$roledPlayerDoClick(int slotId, int button, ClickType clickType, Player player) {
        Inventory inventory = player.getInventory();
        if (clickType == ClickType.QUICK_CRAFT) {
            int i = this.quickcraftStatus;
            this.quickcraftStatus = getQuickcraftHeader(button);
            if ((i != 1 || this.quickcraftStatus != 2) && i != this.quickcraftStatus) {
                this.resetQuickCraft();
            } else if (this.getCarried().isEmpty()) {
                this.resetQuickCraft();
            } else if (this.quickcraftStatus == 0) {
                this.quickcraftType = getQuickcraftType(button);
                if (isValidQuickcraftType(this.quickcraftType, player)) {
                    this.quickcraftStatus = 1;
                    this.quickcraftSlots.clear();
                } else {
                    this.resetQuickCraft();
                }
            } else if (this.quickcraftStatus == 1) {
                Slot slot = this.slots.get(slotId);
                ItemStack itemstack = this.getCarried();
                if (canItemQuickReplace(slot, itemstack, true)
                        && slot.mayPlace(itemstack)
                        && (this.quickcraftType == 2 || itemstack.getCount() > this.quickcraftSlots.size())
                        && this.canDragTo(slot)
                        && one_slot_survival$isEligibleItemTypeForRoledPlayer(itemstack, slotId)) {
                    this.quickcraftSlots.add(slot);
                }
            } else if (this.quickcraftStatus == 2) {
                if (!this.quickcraftSlots.isEmpty()) {
                    if (this.quickcraftSlots.size() == 1) {
                        int i1 = this.quickcraftSlots.iterator().next().index;
                        this.resetQuickCraft();
                        this.doClick(i1, this.quickcraftType, ClickType.PICKUP, player);
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
                                && this.canDragTo(slot1)
                                && one_slot_survival$isEligibleItemTypeForRoledPlayer(itemstack1, slot1.index)) {
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
        } else if ((clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) && (button == 0 || button == 1)) {
            ClickAction clickaction = button == 0 ? ClickAction.PRIMARY : ClickAction.SECONDARY;
            if (slotId == -999) {
                if (!this.getCarried().isEmpty()) {
                    if (clickaction == ClickAction.PRIMARY) {
                        player.drop(this.getCarried(), true);
                        this.setCarried(ItemStack.EMPTY);
                    } else {
                        player.drop(this.getCarried().split(1), true);
                    }
                }
            } else if (clickType == ClickType.QUICK_MOVE) {
                if (slotId < 0) {
                    return;
                }

                Slot slot6 = this.slots.get(slotId);
                if (!slot6.mayPickup(player)) {
                    return;
                }

                ItemStack itemstack8 = this.quickMoveStack(player, slotId);

                while (!itemstack8.isEmpty() && ItemStack.isSameItem(slot6.getItem(), itemstack8)) {
                    itemstack8 = this.quickMoveStack(player, slotId);
                }
            } else {
                if (slotId < 0) {
                    return;
                }

                Slot slot7 = this.slots.get(slotId);
                ItemStack itemstack9 = slot7.getItem();
                ItemStack itemstack10 = this.getCarried();
                player.updateTutorialInventoryAction(itemstack10, slot7.getItem(), clickaction);
                if (!this.tryItemClickBehaviourOverride(player, clickaction, slot7, itemstack9, itemstack10)) {
                    if (!ForgeEventFactory.onItemStackedOn(itemstack9, itemstack10, slot7, clickaction, player, createCarriedSlotAccess()))
                        if (itemstack9.isEmpty()) {
                            if (!itemstack10.isEmpty() && one_slot_survival$isEligibleItemTypeForRoledPlayer(itemstack10, slotId)) {
                                int i3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                                this.setCarried(slot7.safeInsert(itemstack10, i3));
                            }
                        } else if (slot7.mayPickup(player)) {
                            if (itemstack10.isEmpty()) {
                                int j3 = clickaction == ClickAction.PRIMARY ? itemstack9.getCount() : (itemstack9.getCount() + 1) / 2;
                                Optional<ItemStack> optional1 = slot7.tryRemove(j3, Integer.MAX_VALUE, player);
                                optional1.ifPresent(p_150421_ -> {
                                    this.setCarried(p_150421_);
                                    slot7.onTake(player, p_150421_);
                                });
                            } else if (slot7.mayPlace(itemstack10) && one_slot_survival$isEligibleItemTypeForRoledPlayer(itemstack10, slotId)) {
                                if (ItemStack.isSameItemSameComponents(itemstack9, itemstack10)) {
                                    int k3 = clickaction == ClickAction.PRIMARY ? itemstack10.getCount() : 1;
                                    this.setCarried(slot7.safeInsert(itemstack10, k3));
                                } else if (itemstack10.getCount() <= slot7.getMaxStackSize(itemstack10)) {
                                    this.setCarried(itemstack9);
                                    slot7.setByPlayer(itemstack10);
                                }
                            } else if (ItemStack.isSameItemSameComponents(itemstack9, itemstack10)) {
                                Optional<ItemStack> optional = slot7.tryRemove(itemstack9.getCount(), itemstack10.getMaxStackSize() - itemstack10.getCount(), player);
                                optional.ifPresent(p_150428_ -> {
                                    itemstack10.grow(p_150428_.getCount());
                                    slot7.onTake(player, p_150428_);
                                });
                            }
                        }
                }

                slot7.setChanged();
            }
        } else if (clickType == ClickType.SWAP && (button >= 0 && button < 9 || button == 40)) {
            ItemStack itemstack2 = inventory.getItem(button);
            Slot slot5 = this.slots.get(slotId);
            ItemStack itemstack7 = slot5.getItem();
            if (!itemstack2.isEmpty() || !itemstack7.isEmpty()) {
                if (itemstack2.isEmpty()) {
                    if (slot5.mayPickup(player)) {
                        inventory.setItem(button, itemstack7);
                        ((SlotAccessor) slot5).invokeOnSwapCraft(itemstack7.getCount());
                        slot5.setByPlayer(ItemStack.EMPTY);
                        slot5.onTake(player, itemstack7);
                    }
                } else if (itemstack7.isEmpty()) {
                    if (slot5.mayPlace(itemstack2) && one_slot_survival$isEligibleItemTypeForRoledPlayer(itemstack2, slotId)) {
                        int j2 = slot5.getMaxStackSize(itemstack2);
                        if (itemstack2.getCount() > j2) {
                            slot5.setByPlayer(itemstack2.split(j2));
                        } else {
                            inventory.setItem(button, ItemStack.EMPTY);
                            slot5.setByPlayer(itemstack2);
                        }
                    }
                } else if (slot5.mayPickup(player) && slot5.mayPlace(itemstack2) && one_slot_survival$isEligibleItemTypeForRoledPlayer(itemstack2, slotId)) {
                    int k2 = slot5.getMaxStackSize(itemstack2);
                    if (itemstack2.getCount() > k2) {
                        slot5.setByPlayer(itemstack2.split(k2));
                        slot5.onTake(player, itemstack7);
                        if (!inventory.add(itemstack7)) {
                            player.drop(itemstack7, true);
                        }
                    } else {
                        inventory.setItem(button, itemstack7);
                        slot5.setByPlayer(itemstack2);
                        slot5.onTake(player, itemstack7);
                    }
                }
            }
        } else if (clickType == ClickType.CLONE && player.hasInfiniteMaterials() && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot4 = this.slots.get(slotId);
            if (slot4.hasItem()) {
                ItemStack itemstack5 = slot4.getItem();
                this.setCarried(itemstack5.copyWithCount(itemstack5.getMaxStackSize()));
            }
        } else if (clickType == ClickType.THROW && this.getCarried().isEmpty() && slotId >= 0) {
            Slot slot3 = this.slots.get(slotId);
            int j1 = button == 0 ? 1 : slot3.getItem().getCount();
            if (!player.canDropItems()) {
                return;
            }

            ItemStack itemstack6 = slot3.safeTake(j1, Integer.MAX_VALUE, player);
            player.drop(itemstack6, true);
            player.handleCreativeModeItemDrop(itemstack6);
            if (button == 1) {
                while (!itemstack6.isEmpty() && ItemStack.isSameItem(slot3.getItem(), itemstack6)) {
                    if (!player.canDropItems()) {
                        return;
                    }

                    itemstack6 = slot3.safeTake(j1, Integer.MAX_VALUE, player);
                    player.drop(itemstack6, true);
                    player.handleCreativeModeItemDrop(itemstack6);
                }
            }
        } else if (clickType == ClickType.PICKUP_ALL && slotId >= 0) {
            Slot slot2 = this.slots.get(slotId);
            ItemStack itemstack4 = this.getCarried();
            if (!itemstack4.isEmpty() && (!slot2.hasItem() || !slot2.mayPickup(player))) {
                int l1 = button == 0 ? 0 : this.slots.size() - 1;
                int i2 = button == 0 ? 1 : -1;

                for (int l2 = 0; l2 < 2; l2++) {
                    for (int l3 = l1; l3 >= 0 && l3 < this.slots.size() && itemstack4.getCount() < itemstack4.getMaxStackSize(); l3 += i2) {
                        Slot slot8 = this.slots.get(l3);
                        if (slot8.hasItem() && canItemQuickReplace(slot8, itemstack4, true) && slot8.mayPickup(player) && this.canTakeItemForPickAll(itemstack4, slot8)) {
                            ItemStack itemstack11 = slot8.getItem();
                            if (l2 != 0 || itemstack11.getCount() != itemstack11.getMaxStackSize()) {
                                ItemStack itemstack12 = slot8.safeTake(itemstack11.getCount(), itemstack4.getMaxStackSize() - itemstack4.getCount(), player);
                                itemstack4.grow(itemstack12.getCount());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * ロールスロットルールにおいて、新しいアイテムの割り当てとして適切か判定します。
     *
     * @param item 新しいアイテム。
     * @param slotId itemが格納される予定のスロットのID。
     * @return trueのとき適切（配置可能）。
     */
    @Unique
    private boolean one_slot_survival$isEligibleItemTypeForRoledPlayer(ItemStack item, int slotId) {
        if (one_slot_survival$isInventorySlot(slotId) && this.slots.get(slotId).container instanceof Inventory inventory) {
            return ItemType.isEligibleItemForRoledPlayer(item, one_slot_survival$getInventoryIndex(slotId), inventory.player);
        }

        return true;
    }

    @Unique
    private boolean one_slot_survival$isInventorySlot(int slotId) {
        return one_slot_survival$getInventoryIndex(slotId) > -1;
    }

    @Unique
    private int one_slot_survival$getInventoryIndex(int slotId) {
        // スロットIDの範囲チェック
        if (slotId < 0 || slotId >= this.slots.size()) {
            return -1;
        }

        Slot slot = this.slots.get(slotId);

        // Check if this slot belongs to player inventory
        if (!(slot.container instanceof Inventory)) {
            return -1;
        }

        // Get the index within the player's inventory
        return slot.getContainerSlot();
    }
}
