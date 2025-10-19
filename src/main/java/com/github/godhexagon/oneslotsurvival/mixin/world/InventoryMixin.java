package com.github.godhexagon.oneslotsurvival.mixin.world;

import com.github.godhexagon.oneslotsurvival.world.item.ModItems;
import com.github.godhexagon.oneslotsurvival.world.util.InventoryDefinition;
import com.github.godhexagon.oneslotsurvival.world.util.PlayerModValidity;
import net.minecraft.core.Holder;
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
 * Inventoryクラスに仮想スロット機能を追加するMixin
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
    private int selected;

    @Shadow
    private boolean hasRemainingSpaceForItem(ItemStack p_36015_, ItemStack p_36016_) {
        throw new AssertionError();
    }

    /*

    === ClientEvent.enforceMainHandSelectionで改変しているため不要 ===

    public int getSelectedSlot() {
        return this.selected;
    }

    public void setSelectedSlot(int p_398009_) {
        if (!isHotbarSlot(p_398009_)) {
            throw new IllegalArgumentException("Invalid selected slot");
        } else {
            this.selected = p_398009_;
        }
    }

    public ItemStack getSelectedItem() {
        // ⚠️ CONCERN: this.items.get(this.selected)でロールスロット無視不可（選択されたスロット直接アクセス）
        return this.items.get(this.selected);
    }

    public ItemStack setSelectedItem(ItemStack p_393963_) {
        // ⚠️ CONCERN: this.items.set(this.selected, p_393963_)でロールスロット無視不可（選択されたスロット直接アクセス）
        return this.items.set(this.selected, p_393963_);
    }

    === ===

    === これは定数を返す静的メソッド ===
    public static int getSelectionSize() {
        return 9;
    }

    TODO: Inject
    public NonNullList<ItemStack> getNonEquipmentItems() {
        // ⚠️ CONCERN: this.itemsをそのまま返す。呼び出し元で制御不可能
        return this.items;
    }

    === 防具とオフハンドは関係無い。メインハンドはthis.selectedレイヤーで制御 ===
    public EntityEquipment getEquipment() {
        return this.equipment;
    }

    TODO: Inject
    private boolean hasRemainingSpaceForItem(ItemStack p_36015_, ItemStack p_36016_) {
        // ✅ OK: this.itemsに直接アクセスしない（ItemStackの比較のみ）
        return !p_36015_.isEmpty() && ItemStack.isSameItemSameComponents(p_36015_, p_36016_) && p_36015_.isStackable() && p_36015_.getCount() < this.getMaxStackSize(p_36015_);
    }
*/
    @Inject(method = "getFreeSlot", at = @At("HEAD"), cancellable = true)
    private void onGetFreeSlot(CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player)) {
            cir.cancel();

            for (int i = 0; i < this.items.size(); i++) {
                // ロールスロットでなおかつ空の場合は無視される
                if (InventoryDefinition.isRoleSlot(i) && this.items.get(i).isEmpty()) {
                    continue;
                }
                // 正常な制限スロットは無視される
                if (InventoryDefinition.isDisableSlot(i) && this.items.get(i).is(ModItems.SLOT_BARRIER.get())) {
                    continue;
                }

                // バニラの処理
                if (this.items.get(i).isEmpty()) {
                    cir.setReturnValue(i);
                    return;
                }
            }

            cir.setReturnValue(-1);
        }
    }


/*
    // TODO: Inject
    public void addAndPickItem(ItemStack p_378587_) {
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        // ⚠️ CONCERN: this.items.get(this.selected)でロールスロット無視不可（選択されたスロット直接アクセス）
        if (!this.items.get(this.selected).isEmpty()) {
            int i = this.getFreeSlot();
            if (i != -1) {
                // ⚠️ CONCERN: this.items.set()でロールスロット無視不可（選択されたスロット直接アクセス）
                this.items.set(i, this.items.get(this.selected));
            }
        }
        // ⚠️ CONCERN: this.items.set()でロールスロット無視不可（選択されたスロット直接アクセス）
        this.items.set(this.selected, p_378587_);
    }

    // TODO: Inject
    public void pickSlot(int p_36039_) {
        this.setSelectedSlot(this.getSuitableHotbarSlot());
        // ⚠️ CONCERN: this.items.get/set()でロールスロット無視不可（選択されたスロット・引数スロット直接アクセス）
        ItemStack itemstack = this.items.get(this.selected);
        this.items.set(this.selected, this.items.get(p_36039_));
        this.items.set(p_36039_, itemstack);
    }

    // TODO: Inject
    public static boolean isHotbarSlot(int p_36046_) {
        // ✅ OK: this.itemsに直接アクセスしない（静的メソッド）
        return p_36046_ >= 0 && p_36046_ < 9;
    }

*/

    @Inject(method = "findSlotMatchingItem", at = @At("HEAD"), cancellable = true)
    private void onFindSlotMatchingItem(ItemStack p_36031_, CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player))
        {
            cir.cancel();

            for (int i = 0; i < this.items.size(); i++) {
                // ロールスロットでなおかつ空の場合は無視される
                if (InventoryDefinition.isRoleSlot(i) && this.items.get(i).isEmpty()) {
                    continue;
                }
                // 正常な制限スロットは無視される
                if (InventoryDefinition.isDisableSlot(i) && this.items.get(i).is(ModItems.SLOT_BARRIER.get())) {
                    continue;
                }

                // バニラの処理
                if (!this.items.get(i).isEmpty() && ItemStack.isSameItemSameComponents(p_36031_, this.items.get(i))) {
                    cir.setReturnValue(i);
                    return;
                }
            }

            cir.setReturnValue(-1);
        }
    }
    /*

    === ツール耐久システム・エンチャント・名づけシステムはバニラのままにする ===
    public static boolean isUsableForCrafting(ItemStack p_362871_) {
        // ✅ OK: this.itemsに直接アクセスしない（静的メソッド）
        return !p_362871_.isDamaged() && !p_362871_.isEnchanted() && !p_362871_.has(DataComponents.CUSTOM_NAME);
    }
*/

    @Inject(method = "findSlotMatchingCraftingIngredient", at = @At("HEAD"), cancellable = true)
    private void onFindSlotMatchingCraftingIngredient(Holder<Item> p_363996_, ItemStack p_376934_, CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player))
        {
            cir.cancel();

            for (int i = 0; i < this.items.size(); i++) {
                // ロールスロットでなおかつ空の場合は無視される
                if (InventoryDefinition.isRoleSlot(i) && this.items.get(i).isEmpty()) {
                    continue;
                }
                // 正常な制限スロットは無視される
                if (InventoryDefinition.isDisableSlot(i) && this.items.get(i).is(ModItems.SLOT_BARRIER.get())) {
                    continue;
                }

                // バニラの処理
                ItemStack itemstack = this.items.get(i);
                if (!itemstack.isEmpty()
                        && itemstack.is(p_363996_)
                        && Inventory.isUsableForCrafting(itemstack)
                        && (p_376934_.isEmpty() || ItemStack.isSameItemSameComponents(p_376934_, itemstack))) {
                    cir.setReturnValue(i);
                    return;
                }
            }

            cir.setReturnValue(-1);
        }
    }
    /*

*/
    @Inject(method = "getSuitableHotbarSlot", at = @At("HEAD"), cancellable = true)
    private void onGetSuitableHotbarSlot(CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player))
        {
            cir.cancel();

            for (int i = 0; i < 9; i++) {
                int j = (this.selected + i) % 9;
                // ロールスロットでなおかつ空の場合は無視される
                if (InventoryDefinition.isRoleSlot(j) && this.items.get(j).isEmpty()) {
                    continue;
                }
                // 正常な制限スロットは無視される
                if (InventoryDefinition.isDisableSlot(j) && this.items.get(j).is(ModItems.SLOT_BARRIER.get())) {
                    continue;
                }

                // バニラの処理
                if (this.items.get(j).isEmpty()) {
                    cir.setReturnValue(j);
                    return;
                }
            }

            for (int k = 0; k < 9; k++) {
                int l = (this.selected + k) % 9;
                // ロールスロットでなおかつ空の場合は無視される
                if (InventoryDefinition.isRoleSlot(l) && this.items.get(l).isEmpty()) {
                    continue;
                }
                // 正常な制限スロットは無視される
                if (InventoryDefinition.isDisableSlot(l) && this.items.get(l).is(ModItems.SLOT_BARRIER.get())) {
                    continue;
                }

                // バニラの処理
                if (!this.items.get(l).isNotReplaceableByPickAction(this.player, l)) {
                    cir.setReturnValue(l);
                    return;
                }
            }

            cir.setReturnValue(this.selected);
        }
    }
/*

    // TODO: Inject
    public int clearOrCountMatchingItems(Predicate<ItemStack> p_36023_, int p_36024_, Container p_36025_) {
        // ⚠️ COMPLEX: ContainerHelper.clearOrCountMatchingItems(this, ...)でthis(Inventory)を渡している
        // ContainerHelperが内部でthis.itemsをどう扱うか不明
        int i = 0;
        boolean flag = p_36024_ == 0;
        i += ContainerHelper.clearOrCountMatchingItems(this, p_36023_, p_36024_ - i, flag);
        i += ContainerHelper.clearOrCountMatchingItems(p_36025_, p_36023_, p_36024_ - i, flag);
        ItemStack itemstack = this.player.containerMenu.getCarried();
        i += ContainerHelper.clearOrCountMatchingItems(itemstack, p_36023_, p_36024_ - i, flag);
        if (itemstack.isEmpty()) {
            this.player.containerMenu.setCarried(ItemStack.EMPTY);
        }

        return i;
    }

    // TODO: わからん
    private int addResource(ItemStack p_36067_) {
        // ✅ OK: this.itemsに直接アクセスしない（他のメソッドを呼び出すのみ）
        int i = this.getSlotWithRemainingSpace(p_36067_);
        if (i == -1) {
            i = this.getFreeSlot();
        }

        return i == -1 ? p_36067_.getCount() : this.addResource(i, p_36067_);
    }

    // TODO: わからん
    private int addResource(int p_36048_, ItemStack p_36049_) {
        // ⚠️ CONCERN: this.setItem()を呼び出している。setItem()は直接this.items.set()する
        // ⚠️ VALUE_LOSS: p_36049_.copyWithCount(0)でアイテムカウントがリセットされる
        int i = p_36049_.getCount();
        ItemStack itemstack = this.getItem(p_36048_);
        if (itemstack.isEmpty()) {
            itemstack = p_36049_.copyWithCount(0);
            this.setItem(p_36048_, itemstack);
        }

        int j = this.getMaxStackSize(itemstack) - itemstack.getCount();
        int k = Math.min(i, j);
        if (k == 0) {
            return i;
        } else {
            i -= k;
            itemstack.grow(k);
            itemstack.setPopTime(5);
            return i;
        }
    }
*/
    @Inject(method = "getSlotWithRemainingSpace", at = @At("HEAD"), cancellable = true)
    private void onGetSlotWithRemainingSpace(ItemStack p_36051_, CallbackInfoReturnable<Integer> cir) {
        if (PlayerModValidity.isEffective(this.player))
        {
            cir.cancel();

            Inventory inv = (Inventory)(Object)this;

            // バニラの処理: this.selectedと40(オフハンド)は直接チェック
            if (hasRemainingSpaceForItem(inv.getItem(this.selected), p_36051_)) {
                cir.setReturnValue(this.selected);
                return;
            } else if (hasRemainingSpaceForItem(inv.getItem(40), p_36051_)) {
                cir.setReturnValue(40);
                return;
            } else {
                for (int i = 0; i < this.items.size(); i++) {
                    // ロールスロットでなおかつ空の場合は無視される
                    if (InventoryDefinition.isRoleSlot(i) && this.items.get(i).isEmpty()) {
                        continue;
                    }
                    // 正常な制限スロットは無視される
                    if (InventoryDefinition.isDisableSlot(i) && this.items.get(i).is(ModItems.SLOT_BARRIER.get())) {
                        continue;
                    }

                    // バニラの処理
                    if (hasRemainingSpaceForItem(this.items.get(i), p_36051_)) {
                        cir.setReturnValue(i);
                        return;
                    }
                }

                cir.setReturnValue(-1);
            }
        }
    }
/*

    === ItemStack.inventoryTickは実行しておいたほうがよさそう ===
    public void tick() {
        // ✅ OK: ロールスロットのアイテムもtickを実行すべき（アイテムの状態更新のため）
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemstack = this.getItem(i);
            if (!itemstack.isEmpty()) {
                itemstack.inventoryTick(this.player.level(), this.player, i == this.selected ? EquipmentSlot.MAINHAND : null, i);
            }
        }
    }

    === オーバーライド先で変更する ===
    public boolean add(ItemStack p_36055_) {
        // ✅ OK: this.itemsに直接アクセスしない（他のメソッドを呼び出すのみ）
        return this.add(-1, p_36055_);
    }

    // TODO: Inject
    public boolean add(int p_36041_, ItemStack p_36042_) {
        // ⚠️ CONCERN: this.items.set()を直接呼び出している
        // ⚠️ VALUE_LOSS: p_36042_.copyAndClear()で元のアイテムがクリアされる
        // ⚠️ VALUE_LOSS: p_36042_.setCount(0)で無限素材時にアイテムカウントが0になる
        if (p_36042_.isEmpty()) {
            return false;
        } else {
            try {
                if (p_36042_.isDamaged()) {
                    if (p_36041_ == -1) {
                        p_36041_ = this.getFreeSlot();
                    }

                    if (p_36041_ >= 0) {
                        this.items.set(p_36041_, p_36042_.copyAndClear());
                        this.items.get(p_36041_).setPopTime(5);
                        return true;
                    } else if (this.player.hasInfiniteMaterials()) {
                        p_36042_.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;
                    do {
                        i = p_36042_.getCount();
                        if (p_36041_ == -1) {
                            p_36042_.setCount(this.addResource(p_36042_));
                        } else {
                            p_36042_.setCount(this.addResource(p_36041_, p_36042_));
                        }
                    } while (!p_36042_.isEmpty() && p_36042_.getCount() < i);

                    if (p_36042_.getCount() == i && this.player.hasInfiniteMaterials()) {
                        p_36042_.setCount(0);
                        return true;
                    } else {
                        return p_36042_.getCount() < i;
                    }
                }
            } catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.forThrowable(throwable, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being added");
                crashreportcategory.setDetail("Registry Name", () -> String.valueOf(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(p_36042_.getItem())));
                crashreportcategory.setDetail("Item Class", () -> p_36042_.getItem().getClass().getName());
                crashreportcategory.setDetail("Item ID", Item.getId(p_36042_.getItem()));
                crashreportcategory.setDetail("Item data", p_36042_.getDamageValue());
                crashreportcategory.setDetail("Item name", () -> p_36042_.getHoverName().getString());
                throw new ReportedException(crashreport);
            }
        }
    }

    === オーバーライド先で変更する ===
    public void placeItemBackInInventory(ItemStack p_150080_) {
        // ✅ OK: this.itemsに直接アクセスしない（他のメソッドを呼び出すのみ）
        this.placeItemBackInInventory(p_150080_, true);
    }

    // TODO: Inject
    public void placeItemBackInInventory(ItemStack p_150077_, boolean p_150078_) {
        // ⚠️ VALUE_LOSS: p_150077_.split(j)でアイテムが分割される
        while (!p_150077_.isEmpty()) {
            int i = this.getSlotWithRemainingSpace(p_150077_);
            if (i == -1) {
                i = this.getFreeSlot();
            }

            if (i == -1) {
                this.player.drop(p_150077_, false);
                break;
            }

            int j = p_150077_.getMaxStackSize() - this.getItem(i).getCount();
            if (this.add(i, p_150077_.split(j)) && p_150078_ && this.player instanceof ServerPlayer serverplayer) {
                serverplayer.connection.send(this.createInventoryUpdatePacket(i));
            }
        }
    }

    // TODO: ClientboundSetPlayerInventoryPacketの実装を確認する
    public ClientboundSetPlayerInventoryPacket createInventoryUpdatePacket(int p_362278_) {
        // ✅ OK: this.itemsに直接アクセスしない（getItem()を呼び出すのみ）
        return new ClientboundSetPlayerInventoryPacket(p_362278_, this.getItem(p_362278_).copy());
    }

    @Override
    public ItemStack removeItem(int p_35993_, int p_35994_) {
        // ⚠️ COMPLEX: ContainerHelper.removeItem(this.items, ...)でthis.itemsを渡している
        // ContainerHelperが内部でthis.itemsをどう扱うか不明
        // ⚠️ VALUE_LOSS: itemstack.split(p_35994_)でアイテムが分割される
        if (p_35993_ < this.items.size()) {
            return ContainerHelper.removeItem(this.items, p_35993_, p_35994_);
        } else {
            EquipmentSlot equipmentslot = EQUIPMENT_SLOT_MAPPING.get(p_35993_);
            if (equipmentslot != null) {
                ItemStack itemstack = this.equipment.get(equipmentslot);
                if (!itemstack.isEmpty()) {
                    return itemstack.split(p_35994_);
                }
            }

            return ItemStack.EMPTY;
        }
    }

    public void removeItem(ItemStack p_36058_) {
        // ⚠️ CONCERN: this.items.get/set()を直接呼び出している
        // ⚠️ VALUE_LOSS: this.items.set(i, ItemStack.EMPTY)でアイテムが削除される
        for (int i = 0; i < this.items.size(); i++) {
            if (this.items.get(i) == p_36058_) {
                this.items.set(i, ItemStack.EMPTY);
                return;
            }
        }

        for (EquipmentSlot equipmentslot : EQUIPMENT_SLOT_MAPPING.values()) {
            ItemStack itemstack = this.equipment.get(equipmentslot);
            if (itemstack == p_36058_) {
                this.equipment.set(equipmentslot, ItemStack.EMPTY);
                return;
            }
        }
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_36029_) {
        // ⚠️ CONCERN: this.items.get/set()を直接呼び出している
        // ⚠️ VALUE_LOSS: this.items.set(p_36029_, ItemStack.EMPTY)でアイテムが削除される
        if (p_36029_ < this.items.size()) {
            ItemStack itemstack = this.items.get(p_36029_);
            this.items.set(p_36029_, ItemStack.EMPTY);
            return itemstack;
        } else {
            EquipmentSlot equipmentslot = EQUIPMENT_SLOT_MAPPING.get(p_36029_);
            return equipmentslot != null ? this.equipment.set(equipmentslot, ItemStack.EMPTY) : ItemStack.EMPTY;
        }
    }

    @Override
    public void setItem(int p_35999_, ItemStack p_36000_) {
        // ⚠️ CONCERN: this.items.set()を直接呼び出している
        // ⚠️ VALUE_LOSS: this.items.set(p_35999_, p_36000_)で既存アイテムが上書きされる
        if (p_35999_ < this.items.size()) {
            this.items.set(p_35999_, p_36000_);
        }

        EquipmentSlot equipmentslot = EQUIPMENT_SLOT_MAPPING.get(p_35999_);
        if (equipmentslot != null) {
            this.equipment.set(equipmentslot, p_36000_);
        }
    }

    public void save(ValueOutput.TypedOutputList<ItemStackWithSlot> p_406529_) {
        // ✅ OK: ロールスロットのアイテムも保存すべき（セーブデータのため）
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemstack = this.items.get(i);
            if (!itemstack.isEmpty()) {
                p_406529_.add(new ItemStackWithSlot(i, itemstack));
            }
        }
    }

    public void load(ValueInput.TypedInputList<ItemStackWithSlot> p_409752_) {
        // ⚠️ CONCERN: this.items.clear()で全アイテムが削除される
        // ✅ OK: ロールスロットのアイテムもロードすべき（セーブデータのため）
        this.items.clear();

        for (ItemStackWithSlot itemstackwithslot : p_409752_) {
            if (itemstackwithslot.isValidInContainer(this.items.size())) {
                this.setItem(itemstackwithslot.slot(), itemstackwithslot.stack());
            }
        }
    }

    @Override
    public int getContainerSize() {
        // ✅ OK: this.itemsに直接アクセスしない（size()のみ）
        return this.items.size() + EQUIPMENT_SLOT_MAPPING.size();
    }

    @Override
    public boolean isEmpty() {
        // ✅ OK: ロールスロットのアイテムも空チェックに含めるべき
        for (ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        for (EquipmentSlot equipmentslot : EQUIPMENT_SLOT_MAPPING.values()) {
            if (!this.equipment.get(equipmentslot).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int p_35991_) {
        // ⚠️ CONCERN: this.items.get()を直接呼び出している（インデックス指定アクセス）
        if (p_35991_ < this.items.size()) {
            return this.items.get(p_35991_);
        } else {
            EquipmentSlot equipmentslot = EQUIPMENT_SLOT_MAPPING.get(p_35991_);
            return equipmentslot != null ? this.equipment.get(equipmentslot) : ItemStack.EMPTY;
        }
    }

    @Override
    public Component getName() {
        // ✅ OK: this.itemsに直接アクセスしない
        return Component.translatable("container.inventory");
    }

    public void dropAll() {
        // ⚠️ CONCERN: this.items.get/set()を直接呼び出している
        // ⚠️ VALUE_LOSS: this.items.set(i, ItemStack.EMPTY)でアイテムが削除される
        // ❓ QUESTION: ロールスロットのアイテムもドロップすべきか？
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack itemstack = this.items.get(i);
            if (!itemstack.isEmpty()) {
                this.player.drop(itemstack, true, false);
                this.items.set(i, ItemStack.EMPTY);
            }
        }

        this.equipment.dropAll(this.player);
    }

    @Override
    public void setChanged() {
        // ✅ OK: this.itemsに直接アクセスしない
        this.timesChanged++;
    }

    public int getTimesChanged() {
        // ✅ OK: this.itemsに直接アクセスしない
        return this.timesChanged;
    }

    @Override
    public boolean stillValid(Player p_36009_) {
        // ✅ OK: this.itemsに直接アクセスしない
        return true;
    }

    public boolean contains(ItemStack p_36064_) {
        // ⚠️ COMPLEX: for (ItemStack itemstack : this)はIteratorを使用
        // Inventoryのiteratorがthis.itemsをどう扱うか不明
        // ✅ OK: ロールスロットのアイテムも検索対象に含めるべき
        for (ItemStack itemstack : this) {
            if (!itemstack.isEmpty() && ItemStack.isSameItemSameComponents(itemstack, p_36064_)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(TagKey<Item> p_204076_) {
        // ⚠️ COMPLEX: for (ItemStack itemstack : this)はIteratorを使用
        // ✅ OK: ロールスロットのアイテムも検索対象に含めるべき
        for (ItemStack itemstack : this) {
            if (!itemstack.isEmpty() && itemstack.is(p_204076_)) {
                return true;
            }
        }

        return false;
    }

    public boolean contains(Predicate<ItemStack> p_332183_) {
        // ⚠️ COMPLEX: for (ItemStack itemstack : this)はIteratorを使用
        // ✅ OK: ロールスロットのアイテムも検索対象に含めるべき
        for (ItemStack itemstack : this) {
            if (p_332183_.test(itemstack)) {
                return true;
            }
        }

        return false;
    }

    public void replaceWith(Inventory p_36007_) {
        // ✅ OK: this.itemsに直接アクセスしない（setItem/getItemを呼び出すのみ）
        for (int i = 0; i < this.getContainerSize(); i++) {
            this.setItem(i, p_36007_.getItem(i));
        }

        this.setSelectedSlot(p_36007_.getSelectedSlot());
    }

    @Override
    public void clearContent() {
        // ⚠️ CONCERN: this.items.clear()で全アイテムが削除される
        // ⚠️ VALUE_LOSS: this.items.clear()で全アイテムが削除される
        this.items.clear();
        this.equipment.clear();
    }

    public void fillStackedContents(StackedItemContents p_364670_) {
        // ✅ OK: ロールスロットのアイテムもスタック集計に含めるべき
        for (ItemStack itemstack : this.items) {
            p_364670_.accountSimpleStack(itemstack);
        }
    }

    public ItemStack removeFromSelected(boolean p_182404_) {
        // ✅ OK: this.itemsに直接アクセスしない（他のメソッドを呼び出すのみ）
        ItemStack itemstack = this.getSelectedItem();
        return itemstack.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, p_182404_ ? itemstack.getCount() : 1);
    }
     */
}
