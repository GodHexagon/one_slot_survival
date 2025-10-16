# Minecraftインベントリのスロットマッピング調査結果

## 調査日
2025年10月16日

## 概要

バニラMinecraftのインベントリには**2つの異なるスロット番号体系**が存在します：

1. **Inventory（データ層）**: プレイヤーのインベントリデータ（0-35 + 装備スロット）
2. **InventoryMenu（GUI層）**: インベントリ画面のスロット配置（0-45 + 装備）

## なぜ2つの範囲があるのか

### 問題の発見

`SlotDefinition.isRestrictedSlot()`で以下のように定義されていました：

```java
// インベントリホットバーを制限
if (4 <= index && index <= 9) {
    return true;
}

// ホットバーのもう一つの範囲（バニラの実装、よくわらかん）を制限
if (index >= 40 && index <= 44) {
    return true;
}
```

**疑問**: なぜホットバーに `4-9` と `40-44` の2つの範囲があるのか？

## 調査結果

### 1. Inventory（データ層）のスロット構造

`Inventory.java`（line 30-32）:
```java
public static final int INVENTORY_SIZE = 36;
public static final int SELECTION_SIZE = 9;
public static final int SLOT_OFFHAND = 40;
```

**Inventoryのスロット配置**:
- **0-8**: ホットバー（Hotbar）
- **9-35**: メインインベントリ（Main Inventory）
- **36-39**: 防具スロット（Armor Slots）
  - 36: ブーツ (FEET)
  - 37: レギンス (LEGS)
  - 38: チェストプレート (CHEST)
  - 39: ヘルメット (HEAD)
- **40**: オフハンド（Offhand）
- **41**: ボディアーマー（Body Armor）
- **42**: サドル（Saddle）

### 2. InventoryMenu（GUI層）のスロット構造

`InventoryMenu.java`（line 14-28）:
```java
public static final int RESULT_SLOT = 0;
public static final int CRAFT_SLOT_START = 1;
public static final int CRAFT_SLOT_END = 5;
public static final int ARMOR_SLOT_START = 5;
public static final int ARMOR_SLOT_END = 9;
public static final int INV_SLOT_START = 9;
public static final int INV_SLOT_END = 36;
public static final int USE_ROW_SLOT_START = 36;  // ← ホットバー！
public static final int USE_ROW_SLOT_END = 45;
public static final int SHIELD_SLOT = 45;
```

さらに、`InventoryMenu.isHotbarSlot()`（line 69-71）:
```java
public static boolean isHotbarSlot(int p_150593_) {
    return p_150593_ >= 36 && p_150593_ < 45 || p_150593_ == 45;
}
```

**InventoryMenuのスロット配置**:
- **0**: クラフト結果スロット
- **1-4**: 2×2クラフトグリッド
- **5-8**: 防具スロット（HEAD, CHEST, LEGS, FEET の順）
- **9-35**: メインインベントリ（27スロット）
- **36-44**: ホットバー（9スロット） ← **ここが重要！**
- **45**: オフハンド（シールドスロット）

### 3. マッピングの関係

`AbstractContainerMenu.addStandardInventorySlots()`は以下の順序でスロットを追加します：

1. メインインベントリ（Inventory 9-35 → Menu 9-35）
2. ホットバー（Inventory 0-8 → Menu 36-44）

つまり：

| Inventory Index | Item Type | InventoryMenu Index |
|----------------|-----------|---------------------|
| 0-8            | ホットバー    | 36-44               |
| 9-35           | メインインベントリ | 9-35                |
| 36-39          | 防具        | 5-8                 |
| 40             | オフハンド    | 45                  |

## 答え：なぜ2つの範囲があるのか

**ホットバーは2つの異なるコンテキストで異なるインデックスを持つ**：

1. **Inventory（データ層）では `0-8`**
   - プレイヤーのインベントリデータとして保存
   - `Inventory.getItem(index)` で直接アクセス
   - ゲーム内ロジックで使用

2. **InventoryMenu（GUI層）では `36-44`**
   - インベントリ画面でのスロット配置
   - `AbstractContainerMenu.clicked(slotId, ...)` の `slotId` として渡される
   - クラフトグリッドや防具スロットの後に配置されるため、オフセットが発生

## isRestrictedSlot() の正しい理解

```java
// インベントリホットバーを制限（データ層）
if (4 <= index && index <= 9) {
    return true;
}

// ホットバーのもう一つの範囲（GUI層）を制限
if (index >= 40 && index <= 44) {
    return true;
}
```

- **`4-9`**: Inventory層でのホットバー（実際には0-8だが、0-3はメインハンドとロールスロット）
- **`40-44`**: InventoryMenu層でのホットバー（36-44のうち、40-44部分）

**ただし、ロールスロットの実装では**:

```java
public static boolean isRoleSlot(int index) {
    // Inventory層: 1-3
    // InventoryMenu層: 37-39（防具スロットの位置！）
    return (1 <= index && index <= 3) || (37 <= index && index <= 39);
}
```

**重要な発見**: `37-39`はInventoryMenu層では**防具スロット**です！
- 37: レギンス
- 38: チェストプレート
- 39: ヘルメット

つまり、ロールスロットをクリックしたとき：
- **Inventory画面（InventoryMenu）** でクリック → `slotId = 37-39`（防具スロットの位置）
- **その他のコンテナ** でクリック → `slotId = 1-3`（Inventory層の直接インデックス）

## 結論

Minecraftのインベントリシステムは**レイヤー化されたアーキテクチャ**を持っており、各レイヤーで異なるスロット番号を使用します：

1. **データ層（Inventory）**: アイテムの実際の保存場所（0-8がホットバー）
2. **GUI層（InventoryMenu）**: 画面表示とユーザー操作（36-44がホットバー、5-8が防具）

ロールスロットも同様に：
- **データ層**: インデックス 1-3
- **GUI層**: インデックス 37-39（防具スロットエリア）

この設計により、異なるGUI（クリエイティブモード、チェスト、作業台など）で異なるスロット配置を実現しながら、バックエンドのInventoryデータ構造は統一されています。

## 参考資料

- `net.minecraft.world.entity.player.Inventory` - データ層の実装
- `net.minecraft.world.inventory.InventoryMenu` - プレイヤーインベントリGUIの実装
- `net.minecraft.world.inventory.AbstractContainerMenu` - スロット操作の基底クラス
