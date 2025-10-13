# プレイヤーインベントリ拡張 - 調査メモ

**作成日**: 2025年（調査時点）
**目的**: プレイヤーに追加のアイテムスタックを保持させる最適な改変方法を見つける

---

## バニラのインベントリ構造

### Player.java

**場所**: `net.minecraft.world.entity.player.Player.java:179`

```java
final Inventory inventory;  // ⚠️ final なので直接置き換え不可
protected PlayerEnderChestContainer enderChestInventory;
public final InventoryMenu inventoryMenu;
public AbstractContainerMenu containerMenu;
```

**重要な発見**:
- `inventory`は**finalフィールド**
- コンストラクタで `new Inventory(this, this.equipment)` として初期化 (line 227)
- Forgeは`PlayerMainInvWrapper`/`PlayerEquipmentInvWrapper`でIItemHandlerとして公開 (lines 232-240)

### Inventory.java

**場所**: `net.minecraft.world.entity.player.Inventory.java`

**コアデータ構造**:
```java
private final NonNullList<ItemStack> items = NonNullList.withSize(36, ItemStack.EMPTY); // line 54
public final Player player;
private final EntityEquipment equipment;
```

**定数**:
- `INVENTORY_SIZE = 36` (ホットバー9 + メイン27)
- `SLOT_OFFHAND = 40`
- `SLOT_BODY_ARMOR = 41`
- `SLOT_SADDLE = 42`

**スロットマッピング**:
```java
// line 36-53
EQUIPMENT_SLOT_MAPPING = Map.of(
    36 + offset -> EquipmentSlot.FEET,
    36 + offset -> EquipmentSlot.LEGS,
    36 + offset -> EquipmentSlot.CHEST,
    36 + offset -> EquipmentSlot.HEAD,
    40 -> EquipmentSlot.OFFHAND,
    41 -> EquipmentSlot.BODY,
    42 -> EquipmentSlot.SADDLE
)
```

**合計サイズ**:
```java
public int getContainerSize() {
    return this.items.size() + EQUIPMENT_SLOT_MAPPING.size(); // 36 + 7 = 43
}
```

**永続化**:
```java
// save: line 388-395
public void save(ValueOutput.TypedOutputList<ItemStackWithSlot> p_406529_) {
    for (int i = 0; i < this.items.size(); i++) {
        ItemStack itemstack = this.items.get(i);
        if (!itemstack.isEmpty()) {
            p_406529_.add(new ItemStackWithSlot(i, itemstack));
        }
    }
}

// load: line 397-405
public void load(ValueInput.TypedInputList<ItemStackWithSlot> p_409752_) {
    this.items.clear();
    for (ItemStackWithSlot itemstackwithslot : p_409752_) {
        if (itemstackwithslot.isValidInContainer(this.items.size())) {
            this.setItem(itemstackwithslot.slot(), itemstackwithslot.stack());
        }
    }
}
```

### InventoryMenu.java

**場所**: `net.minecraft.world.inventory.InventoryMenu.java`

**役割**: UI層 - GUIとデータ間の橋渡し

**重要な発見**:
- すべてのスロットは`Container`を参照する (line 55: `new Slot(inventory, 40, 77, 62)`)
- `addStandardInventorySlots()` でメインインベントリ36スロットを追加 (line 54)
- オフハンドスロット追加例 (line 55-66)
- **永続化なし** - inventoryを参照しているだけ

**結論**: InventoryMenuだけでは不十分。スロットが参照する**データの保存場所**が必須。

---

## Forgeの拡張機構

### 1. Capability システム (Forge 1.21.8)

**参考**: https://docs.minecraftforge.net/en/1.21.x/datastorage/capabilities/

**特徴**:
- Entity/BlockEntity/ItemStack/Level/LevelChunkに対応
- プレイヤーは`AttachCapabilitiesEvent<Entity>`でアタッチ
- **制限**:
  - ❌ デフォルトで死亡時にデータ消失 → `PlayerEvent.Clone`で対処必要
  - ❌ クライアント同期は手動（カスタムパケット実装必要）

**基本フロー**:
```java
// 1. Capability定義
@AutoRegisterCapability
public interface IPlayerInventoryCapability {
    SimpleContainer getAdditionalInventory();
}

// 2. アタッチ
@SubscribeEvent
public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
    if (event.getObject() instanceof Player) {
        event.addCapability(id, provider);
    }
}

// 3. 死亡時のデータコピー
@SubscribeEvent
public void onPlayerClone(PlayerEvent.Clone event) {
    if (event.isWasDeath()) {
        // 旧プレイヤー → 新プレイヤーにデータコピー
    }
}
```

**デメリット**:
- 複雑な実装（アタッチ、死亡時処理、同期パケット）
- 既存のInventory APIと統合が弱い

### 2. Attributeシステム

**参考**: `docs/attribute-implementation.md`

**特徴**:
- 数値データのみ（double型）
- 自動永続化、自動クライアント同期
- ItemStack保存には**不適**

---

## 採用戦略: Mixinベースの仮想インベントリ

### なぜMixinか？

**利点**:
1. **既存APIとの完全統合** - `player.getInventory().getItem(slot)`が自然に動く
2. **永続化の自動化** - `save()`/`load()`をMixinすればNBT保存が自動
3. **拡張性** - 独自ロジックをスロットごとに適用可能
4. **互換性** - バニラの動作を壊さない

**比較**:
| アプローチ | 永続化 | 同期 | 既存API統合 | 複雑度 |
|----------|-------|------|-----------|--------|
| Capability | 手動 | 手動 | ❌ | 高 |
| Mixin | 自動 | 自動 | ✅ | 中 |

---

## 設計: 拡張性を重視したMixin実装

### アーキテクチャ

```
┌─────────────────────────────────────┐
│   Inventory (Mixinで拡張)           │
├─────────────────────────────────────┤
│ items (36スロット) [バニラ]         │
│ virtualSlots (N個) [MOD追加]        │
│   ↓ 各スロットは                     │
│   IVirtualSlot インターフェース     │
└─────────────────────────────────────┘
```

### 仮想スロット インターフェース

```java
public interface IVirtualSlot {
    /**
     * このスロットにアイテムを挿入可能か判定
     */
    boolean canInsert(ItemStack stack, Player player);

    /**
     * このスロットからアイテムを取り出し可能か判定
     */
    boolean canExtract(ItemStack stack, Player player);

    /**
     * アイテム挿入時のカスタムロジック
     * @return 実際に挿入されたアイテム（変更可能）
     */
    ItemStack onInsert(ItemStack stack, Player player);

    /**
     * アイテム取り出し時のカスタムロジック
     * @return 実際に取り出されたアイテム（変更可能）
     */
    ItemStack onExtract(ItemStack stack, Player player);

    /**
     * このスロットをNBTに永続化するか
     */
    boolean shouldPersist();

    /**
     * スロットの表示名（デバッグ用）
     */
    Component getDisplayName();
}
```

### Mixin対象メソッド

**必須Mixin一覧**:

#### 1. データ構造の追加
```java
@Mixin(Inventory.class)
public class InventoryMixin {
    @Unique
    private final Map<Integer, IVirtualSlot> virtualSlots = new HashMap<>();

    @Unique
    private final NonNullList<ItemStack> virtualItems = NonNullList.create();
}
```

#### 2. スロットアクセス（6メソッド）
- `getContainerSize()` - 合計サイズを返す
- `getItem(int slot)` - 仮想スロット範囲なら virtualItems から取得
- `setItem(int slot, ItemStack stack)` - 仮想スロット範囲なら virtualItems に設定
- `removeItem(int slot, int count)` - 仮想スロットからも削除
- `removeItemNoUpdate(int slot)` - 仮想スロットからも削除
- `isEmpty()` - 仮想スロットも確認

**実装パターン例**:
```java
@Inject(method = "getContainerSize", at = @At("RETURN"), cancellable = true)
private void onGetContainerSize(CallbackInfoReturnable<Integer> cir) {
    cir.setReturnValue(cir.getReturnValue() + virtualSlots.size());
}

@Inject(method = "getItem", at = @At("HEAD"), cancellable = true)
private void onGetItem(int slot, CallbackInfoReturnable<ItemStack> cir) {
    if (isVirtualSlot(slot)) {
        int virtualIndex = slot - VANILLA_SIZE;
        cir.setReturnValue(virtualItems.get(virtualIndex));
    }
}

@Inject(method = "setItem", at = @At("HEAD"), cancellable = true)
private void onSetItem(int slot, ItemStack stack, CallbackInfo ci) {
    if (isVirtualSlot(slot)) {
        int virtualIndex = slot - VANILLA_SIZE;
        IVirtualSlot virtualSlot = virtualSlots.get(virtualIndex);

        // カスタムロジック適用
        if (virtualSlot.canInsert(stack, player)) {
            ItemStack modified = virtualSlot.onInsert(stack, player);
            virtualItems.set(virtualIndex, modified);
        }
        ci.cancel();
    }
}
```

#### 3. 永続化（2メソッド）
```java
@Inject(method = "save", at = @At("RETURN"))
private void onSave(ValueOutput.TypedOutputList<ItemStackWithSlot> list, CallbackInfo ci) {
    for (int i = 0; i < virtualSlots.size(); i++) {
        IVirtualSlot slot = virtualSlots.get(i);
        if (slot.shouldPersist()) {
            ItemStack stack = virtualItems.get(i);
            if (!stack.isEmpty()) {
                int globalSlot = VANILLA_SIZE + i;
                list.add(new ItemStackWithSlot(globalSlot, stack));
            }
        }
    }
}

@Inject(method = "load", at = @At("RETURN"))
private void onLoad(ValueInput.TypedInputList<ItemStackWithSlot> list, CallbackInfo ci) {
    for (ItemStackWithSlot item : list) {
        int slot = item.slot();
        if (isVirtualSlot(slot)) {
            int virtualIndex = slot - VANILLA_SIZE;
            virtualItems.set(virtualIndex, item.stack());
        }
    }
}
```

#### 4. アイテム操作（2メソッド）
- `add(ItemStack)` - 仮想スロットへの追加ルール適用
- `getFreeSlot()` - 仮想スロットも検索対象に含める

#### 5. 検索系（3メソッド）
- `contains(ItemStack)` - 仮想スロットも検索
- `contains(TagKey<Item>)` - 仮想スロットも検索
- `contains(Predicate<ItemStack>)` - 仮想スロットも検索

#### 6. 一括操作（2メソッド）
- `dropAll()` - 仮想スロットもドロップ
- `clearContent()` - 仮想スロットもクリア

---

## 実装例：ロールスロット

```java
public class RoleSlot implements IVirtualSlot {
    private final Predicate<ItemStack> acceptedItems;
    private final boolean persistent;

    public RoleSlot(Predicate<ItemStack> acceptedItems, boolean persistent) {
        this.acceptedItems = acceptedItems;
        this.persistent = persistent;
    }

    @Override
    public boolean canInsert(ItemStack stack, Player player) {
        return acceptedItems.test(stack);
    }

    @Override
    public boolean canExtract(ItemStack stack, Player player) {
        return true; // 常に取り出し可能
    }

    @Override
    public ItemStack onInsert(ItemStack stack, Player player) {
        // カスタムロジック: 例えばエフェクト付与
        player.displayClientMessage(
            Component.literal("Role item equipped!"),
            true
        );
        return stack;
    }

    @Override
    public ItemStack onExtract(ItemStack stack, Player player) {
        return stack;
    }

    @Override
    public boolean shouldPersist() {
        return persistent;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Role Slot");
    }
}
```

---

## 次のステップ

1. **Mixin環境の確認** - `build.gradle`, `mixin config` の確認
2. **Mixinの実装** - `InventoryMixin.java` 作成
3. **インターフェース実装** - `IVirtualSlot.java` と具体実装
4. **テスト** - ゲーム内で動作確認

---

## 参考ファイル

- Player.java: `net/minecraft/world/entity/player/Player.java:179`
- Inventory.java: `net/minecraft/world/entity/player/Inventory.java`
- InventoryMenu.java: `net/minecraft/world/inventory/InventoryMenu.java`
- PlayerMainInvWrapper.java: `net/minecraftforge/items/wrapper/PlayerMainInvWrapper.java`
- Forge Capability Docs: https://docs.minecraftforge.net/en/1.21.x/datastorage/capabilities/
