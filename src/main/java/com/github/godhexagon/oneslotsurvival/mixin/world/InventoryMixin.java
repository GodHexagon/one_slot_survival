package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.util.inventory.InventoryDefinition;
import com.github.godhexagon.oneslotsurvival.world.util.inventory.SlotRestriction;
import com.github.godhexagon.oneslotsurvival.world.util.player.PlayerModValidity;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
    public abstract ItemStack getItem(int slot);
    
    @Shadow
    private int selected;
    
    @Shadow
    private boolean hasRemainingSpaceForItem(ItemStack p_36015_, ItemStack p_36016_) {
        throw new AssertionError();
    }
    
    @Shadow
    private int addResource(int p_36048_, ItemStack p_36049_) {
        throw new AssertionError();
    }

    @Shadow
    public abstract int getSuitableHotbarSlot();

    @Shadow
    public abstract void setSelectedSlot(int slot);

    /**
     * アイテム拾得などで呼ばれる add(int, ItemStack) をインターセプトし、
     * ロールスロット制限を適用します。
     *
     * add(ItemStack)はこのメソッドを呼び出すだけなので、
     * より本質的なこちらのメソッドをインターセプトします。
     */
    @Inject(method = "add(ILnet/minecraft/world/item/ItemStack;)Z", at = @At("HEAD"), cancellable = true)
    private void onAdd(int slot, ItemStack itemStack, CallbackInfoReturnable<Boolean> cir) {
        if (PlayerModValidity.isEffective(this.player)) {
            cir.cancel();

            boolean result = one_slot_survival$roledPlayerAdd(slot, itemStack);
            cir.setReturnValue(result);
        }
    }

    /**
     * インベントリを閉じた際にマウスに保持していたアイテムをインベントリに戻す処理をインターセプト。
     * ロールスロット制限に対応したメソッドを使用します。
     */
    @Inject(method = "placeItemBackInInventory(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("HEAD"), cancellable = true)
    private void onPlaceItemBackInInventory(ItemStack itemStack, boolean sendPacket, CallbackInfo ci) {
        if (PlayerModValidity.isEffective(this.player)) {
            ci.cancel();
            one_slot_survival$roledPlayerPlaceItemBackInInventory(itemStack, sendPacket);
        }
    }

    /**
     * MODが有効なプレイヤー専用のaddメソッド。
     * バニラの Inventory.add(int, ItemStack) の完全コピー + ★マークで改変箇所を明示。
     * 詳細はバニラコードを確認してください。
     * 現在は、RoleSlot.isEligibleItemForRoledPlayer を用いることで、適切でない配置を判定・拒否しています。
     */
    @Unique
    private boolean one_slot_survival$roledPlayerAdd(int slot, ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return false;
        } else {
            try {
                if (itemStack.isDamaged()) {
                    if (slot == -1) {
                        // ★耐久アイテム×探索モードの拒否
                        slot = this.one_slot_survival$getFreeSlot(itemStack);
                    }

                    if (slot >= 0) {
                        // ★耐久アイテム×指定モードの拒否
                        if (!SlotRestriction.isEligibleItemForRoledPlayer(itemStack, slot, this.player)) {
                            return false;
                        }

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
                            // ★スタック可能アイテム×探索モードの拒否
                            itemStack.setCount(this.one_slot_survival$roledPlayerAddResource(itemStack));
                        } else {
                            // ★スタック可能アイテム×指定モードの拒否
                            if (!SlotRestriction.isEligibleItemForRoledPlayer(itemStack, slot, this.player)) {
                                return false;
                            }

                            itemStack.setCount(this.addResource(slot, itemStack));
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
     * バニラの addResource(ItemStack) のコピーですが、代替でロールスロット対応メソッドを呼び出す変更がされています。
     * スタック可能なアイテムをインベントリに追加する際、既存スタックに追加可能か、
     * または新しいスロットに配置できるかを判定します。
     *
     * @param itemStack 追加するアイテム
     * @return 追加後のアイテムのカウント（追加できなかった分）
     */
    @Unique
    private int one_slot_survival$roledPlayerAddResource(ItemStack itemStack) {
        Inventory inventory = (Inventory)(Object)this;
        int i = inventory.getSlotWithRemainingSpace(itemStack);
        if (i == -1) {
            i = this.one_slot_survival$getFreeSlot(itemStack);
        }

        return i == -1 ? itemStack.getCount() : this.addResource(i, itemStack);
    }

    /**
     * バニラの getSlotWithRemainingSpace にロールスロット制限を追加します。
     * アイテムをスタックできる余地があるスロットを探します。
     * 優先順位: 1) 選択中のスロット、2) オフハンドスロット(40)、3) その他のスロット
     * 各スロットがロールスロット制限に適合しているかもチェックします。
     *
     * @param item スタックを探すアイテム
     * @param cir コールバック情報（戻り値を設定）
     */
    @Inject(method = "getSlotWithRemainingSpace", at = @At("HEAD"), cancellable = true)
    private void onGetSlotWithRemainingSpace(ItemStack item, CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player)) {
            if (this.hasRemainingSpaceForItem(this.getItem(this.selected), item) && SlotRestriction.isEligibleItemForRoledPlayer(item, this.selected, this.player)) {
                cir.setReturnValue(this.selected);
            } else if (this.hasRemainingSpaceForItem(this.getItem(40), item) && SlotRestriction.isEligibleItemForRoledPlayer(item, 40, this.player)) {
                cir.setReturnValue(40);
            } else {
                for (int i = 0; i < this.items.size(); i++) {
                    if (this.hasRemainingSpaceForItem(this.items.get(i), item) && SlotRestriction.isEligibleItemForRoledPlayer(item, i, this.player)) {
                        cir.setReturnValue(i);
                        return;
                    }
                }
                cir.setReturnValue(-1);
            }
        }
    }

    /**
     * バニラの getFreeSlot メソッドをインターセプトします。
     * MODが有効なプレイヤーに対しては、処理をキャンセルして独自の処理を実行します。
     * 外部から呼ばれた場合、何のアイテムに対しての探索なのかわからないので、仕方なくロールスロットも含めて制限します。
     */
    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void onGetFreeSlot(CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player)) {
            cir.cancel();
                
            for (int i = 0; i < this.items.size(); i++) {
                if (this.items.get(i).isEmpty() && InventoryDefinition.isRestrictedSlot(i)) {
                    cir.setReturnValue(i);
                    return;
                }
            }

            cir.setReturnValue(-1);
        }
    }

    /**
     * バニラの getFreeSlot の完全コピー + ロールスロット対応版。
     * 空いているスロットを探します。
     * ロールスロット制限により、アイテムが配置可能なスロットのみを返します。
     *
     * @param asItem 配置するアイテム
     * @return 空いているスロット番号、見つからない場合は-1
     */
    @Unique
    public int one_slot_survival$getFreeSlot(ItemStack asItem) {
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i).isEmpty() && SlotRestriction.isEligibleItemForRoledPlayer(asItem, i, this.player)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * バニラの placeItemBackInInventory(ItemStack, boolean) のコピー + ロールスロット対応版。
     * インベントリを閉じた際などに、マウスに保持していたアイテムをインベントリに戻す処理です。
     * ロールスロット制限に対応したメソッドを使用して、適切なスロットにのみ配置します。
     * 配置できない場合は、アイテムをドロップします。
     *
     * @param itemStack インベントリに戻すアイテム
     * @param sendPacket クライアントに更新パケットを送信するかどうか
     */
    @Unique
    private void one_slot_survival$roledPlayerPlaceItemBackInInventory(ItemStack itemStack, boolean sendPacket) {
        Inventory inventory = (Inventory)(Object)this;
        while (!itemStack.isEmpty()) {
            // ロールスロット対応版のメソッドを使用
            int i = inventory.getSlotWithRemainingSpace(itemStack);
            if (i == -1) {
                i = this.one_slot_survival$getFreeSlot(itemStack);
            }

            if (i == -1) {
                // 配置できるスロットがない場合はドロップ
                this.player.drop(itemStack, false);
                break;
            }

            int j = itemStack.getMaxStackSize() - this.getItem(i).getCount();
            if (this.one_slot_survival$roledPlayerAdd(i, itemStack.split(j)) && sendPacket && this.player instanceof ServerPlayer serverplayer) {
                serverplayer.connection.send(inventory.createInventoryUpdatePacket(i));
            }
        }
    }

    /**
     * バニラの addAndPickItem メソッドをインターセプトします。
     * MODが有効なプレイヤーに対しては、処理をキャンセルして独自の処理を実行します。
     * getFreeSlot() の代わりに one_slot_survival$getFreeSlot を使用することで、
     * ロールスロット制限に対応した空きスロット探索を行います。
     */
    @Inject(method = "addAndPickItem", at = @At("HEAD"), cancellable = true)
    private void onAddAndPickItem(ItemStack itemStack, CallbackInfo ci) {
        if (PlayerModValidity.isEffective(this.player)) {
            ci.cancel();
            one_slot_survival$roledPlayerAddAndPickItem(itemStack);
        }
    }

    /**
     * バニラの addAndPickItem の完全コピー + ロールスロット対応版。
     * クリエイティブモードでアイテムを中クリックした際などに、
     * アイテムをホットバーに追加し、そのスロットを選択します。
     * ロールスロット制限に対応した空きスロット探索を使用します。
     *
     * @param itemStack 追加するアイテム
     */
    @Unique
    private void one_slot_survival$roledPlayerAddAndPickItem(ItemStack itemStack) {
        Inventory inventory = (Inventory)(Object)this;
        inventory.setSelectedSlot(inventory.getSuitableHotbarSlot());
        if (!this.items.get(this.selected).isEmpty()) {
            // ★ロールスロット対応版を使用
            int i = this.one_slot_survival$getFreeSlot(itemStack);
            if (i != -1) {
                this.items.set(i, this.items.get(this.selected));
            }
        }

        this.items.set(this.selected, itemStack);
    }
}
