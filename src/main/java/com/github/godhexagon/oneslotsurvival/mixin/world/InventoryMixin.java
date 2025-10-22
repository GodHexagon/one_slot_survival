package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import com.github.godhexagon.oneslotsurvival.world.util.RoleSlot;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Inventoryクラスにロールスロット拾得制限機能を追加するMixin
 */
@Mixin(Inventory.class)
public abstract class InventoryMixin {
    @Shadow
    @Final
    private NonNullList<ItemStack> items;

    @Shadow
    @Final
    public Player player;

    @Shadow
    public abstract int getSlotWithRemainingSpace(ItemStack stack);

    @Shadow
    public abstract int getFreeSlot();

    @Shadow
    public abstract ItemStack getItem(int slot);

    /**
     * アイテム拾得などで呼ばれる add(ItemStack) をインターセプトし、
     * ロールスロット制限を適用します。
     */
    @Inject(method = "add(Lnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onAdd(ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (PlayerModValidity.isEffective(this.player)) {
            cir.cancel();

            try {
                boolean result = one_slot_survival$roledPlayerAdd(itemStack);
                cir.setReturnValue(result);
            } catch (Throwable throwable) {
                // バニラのエラーハンドリングと同じ形式
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being added");
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(itemStack.getItem())));
                crashreportcategory.setDetail("Item Class", () -> itemStack.getItem().getClass().getName());
                crashreportcategory.setDetail("Item ID", Item.getId(itemStack.getItem()));
                crashreportcategory.setDetail("Item data", itemStack.getDamageValue());
                crashreportcategory.setDetail("Item name", () -> itemStack.getHoverName().getString());
                throw new ReportedException(crashreport);
            }
        }
    }

    /**
     * MODが有効なプレイヤー専用のaddメソッド。
     * バニラの Inventory.add(ItemStack) の処理をそのまま持ってきて、これを一部改変しています。
     * 詳細はバニラコードを確認してください。
     * 現在は、RoleSlot.isEligibleItemForRoledPlayer を用いることで、適切でない配置を判定・拒否しています。
     */
    @Unique
    private boolean one_slot_survival$roledPlayerAdd(ItemStack itemStack) {
        return this.one_slot_survival$roledPlayerAdd(-1, itemStack);
    }

    /**
     * MODが有効なプレイヤー専用のaddメソッド（スロット指定版）。
     * バニラの Inventory.add(int, ItemStack) の完全コピー + ★マークで改変箇所を明示。
     */
    @Unique
    private boolean one_slot_survival$roledPlayerAdd(int slot, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else {
            try {
                if (itemStack.isDamaged()) {
                    if (slot == -1) {
                        slot = this.getFreeSlot();
                    }

                    // ★ロールスロット制限チェック
                    if (slot >= 0 && !RoleSlot.isEligibleItemForRoledPlayer(itemStack, slot, this.player)) {
                        // クリエイティブモードの処理
                        if (this.player.hasInfiniteMaterials()) {
                            itemStack.setCount(0);
                            return true;
                        } else {
                            return false;
                        }
                    }

                    if (slot >= 0) {
                        this.items.set(slot, itemStack.copyAndClear());
                        this.items.get(slot).setPopTime(5);
                        return true;
                    } else if (this.player.hasInfiniteMaterials()) {
                        itemStack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;
                    do {
                        i = itemStack.getCount();
                        if (slot == -1) {
                            itemStack.setCount(this.one_slot_survival$roledPlayerAddResource(itemStack));
                        } else {
                            itemStack.setCount(this.one_slot_survival$roledPlayerAddResource(slot, itemStack));
                        }
                    } while (!itemStack.isEmpty() && itemStack.getCount() < i);

                    if (itemStack.getCount() == i && this.player.hasInfiniteMaterials()) {
                        itemStack.setCount(0);
                        return true;
                    } else {
                        return itemStack.getCount() < i;
                    }
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being added");
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(itemStack.getItem())));
                crashreportcategory.setDetail("Item Class", () -> itemStack.getItem().getClass().getName());
                crashreportcategory.setDetail("Item ID", Item.getId(itemStack.getItem()));
                crashreportcategory.setDetail("Item data", itemStack.getDamageValue());
                crashreportcategory.setDetail("Item name", () -> itemStack.getHoverName().getString());
                throw new ReportedException(crashreport);
            }
        }
    }

    /**
     * バニラの addResource(ItemStack) の完全コピー + ★ロールスロット制限追加。
     */
    @Unique
    private int one_slot_survival$roledPlayerAddResource(ItemStack itemStack) {
        int slot = this.getSlotWithRemainingSpace(itemStack);

        // ★ロールスロット制限：既存スタックが不適切なら別のスロットを探す
        if (slot != -1 && !RoleSlot.isEligibleItemForRoledPlayer(itemStack, slot, this.player)) {
            slot = -1; // この場所は使えない
        }

        if (slot == -1) {
            slot = this.getFreeSlot();
            // ★再度チェック
            if (slot != -1 && !RoleSlot.isEligibleItemForRoledPlayer(itemStack, slot, this.player)) {
                slot = -1;
            }
        }

        return slot == -1 ? itemStack.getCount() : this.one_slot_survival$roledPlayerAddResource(slot, itemStack);
    }

    /**
     * バニラの addResource(int, ItemStack) の完全コピー + ★ロールスロット制限追加。
     */
    @Unique
    private int one_slot_survival$roledPlayerAddResource(int slot, ItemStack itemStack) {
        // ★最終チェック
        if (!RoleSlot.isEligibleItemForRoledPlayer(itemStack, slot, this.player)) {
            return itemStack.getCount(); // 追加失敗
        }

        int count = itemStack.getCount();
        ItemStack existingStack = this.getItem(slot);

        if (existingStack.isEmpty()) {
            existingStack = itemStack.copyWithCount(0);
            this.items.set(slot, existingStack);
        }

        // Container.getMaxStackSize(ItemStack)の実装: Math.min(99, itemStack.getMaxStackSize())
        // Inventoryのデフォルト最大スタックサイズは99
        int maxStackSize = Math.min(99, existingStack.getMaxStackSize());
        int maxAdd = maxStackSize - existingStack.getCount();
        int toAdd = Math.min(count, maxAdd);

        if (toAdd == 0) {
            return count;
        } else {
            count -= toAdd;
            existingStack.grow(toAdd);
            existingStack.setPopTime(5);
            return count;
        }
    }
}
