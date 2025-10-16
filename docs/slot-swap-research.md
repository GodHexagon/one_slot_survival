# スロット間アイテム入れ替え処理の調査結果

## 調査日
2025年10月5日

## 概要
バニラMinecraftにおけるスロット間のアイテム入れ替え処理の実装箇所を特定しました。

## 処理フロー

### クライアントサイド
1. **AbstractContainerScreen.slotClicked()**
   - スロットクリックのエントリーポイント
   - パラメータ: `Slot slot, int slotId, int mouseButton, ClickType type`
   - このメソッドはクライアント側でユーザーの入力を受け取る

### サーバーサイド（重要）
2. **AbstractContainerMenu.clicked()**
   - サーバーサイドのスロットクリック処理エントリーポイント
   - パラメータ: `int slotId, int button, ClickType clickType, Player player`
   - このメソッドが内部で `doClick()` を呼び出す

3. **AbstractContainerMenu.doClick()** ← **ここが本体！**
   - プライベートメソッド
   - 実際のアイテム移動ロジックを実装
   - ClickType に応じて異なる処理を実行
   - パラメータ: `int slotId, int button, ClickType clickType, Player player`

### 補助メソッド
4. **AbstractContainerMenu.quickMoveStack()**
   - Shift+クリックでのアイテム移動
   - パラメータ: `Player player, int slotIndex`

5. **AbstractContainerMenu.moveItemStackTo()**
   - スロット範囲間でのアイテム移動ユーティリティ
   - パラメータ: `ItemStack stack, int startSlot, int endSlot, boolean reverseDirection`

## ClickType の種類
- `PICKUP`: 通常の左/右クリック
- `QUICK_MOVE`: Shift+クリック
- `SWAP`: 数字キーでのホットバースワップ
- `CLONE`: クリエイティブモードでのクローン
- `THROW`: Qキーでのアイテム投げ
- `QUICK_CRAFT`: ドラッグでの複数スロット操作
- `PICKUP_ALL`: ダブルクリック

## Mixin でのインターセプト推奨箇所

### クライアントサイド制御
- **AbstractContainerScreen.slotClicked()**
  - クライアント側での視覚的なフィードバック制御
  - ユーザー操作の即座のキャンセル
  - 現在のプロジェクトで実装済み

### サーバーサイド制御（より確実）
- **AbstractContainerMenu.clicked()** または **doClick()**
  - サーバー側での最終的な検証
  - 実際のアイテム移動をブロック
  - クライアント側をバイパスした操作も防げる

## 推奨実装戦略

### ロールスロットのバリアアイテムスワップ実装
1. **AbstractContainerMenu.clicked()** に Mixin を注入
2. ClickType と slotId を確認
3. ロールスロット（インデックス1-3）へのクリックの場合：
   - バリアアイテムと実際のアイテムをスワップ
   - または、バリアアイテムを一時的に除去してから元に戻す

## 参考
- JavaDoc: https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.17.1/net/minecraft/world/inventory/AbstractContainerMenu.html
- NeoForge Documentation: https://github.com/neoforged/Documentation/blob/main/docs/blockentities/container.md

## doClick() の詳細解析結果

### ソースコード所在
- ファイル: `AbstractContainerMenu.java` (line 340)
- 場所: `~/.gradle/caches/forge_gradle/minecraft_user_repo/.../forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar`

### SWAP処理の詳細（line 478-515）

```java
else if (p_150433_ == ClickType.SWAP && (p_150432_ >= 0 && p_150432_ < 9 || p_150432_ == 40)) {
    ItemStack itemstack2 = inventory.getItem(p_150432_);  // ホットバーのアイテム
    Slot slot5 = this.slots.get(p_150431_);               // クリックされたスロット
    ItemStack itemstack7 = slot5.getItem();               // スロットのアイテム

    if (!itemstack2.isEmpty() || !itemstack7.isEmpty()) {
        // ケース1: ホットバーが空、スロットに��イテムあり
        if (itemstack2.isEmpty()) {
            if (slot5.mayPickup(p_150434_)) {
                inventory.setItem(p_150432_, itemstack7);
                slot5.setByPlayer(ItemStack.EMPTY);
                slot5.onTake(p_150434_, itemstack7);
            }
        }
        // ケース2: ホットバーにアイテムあり、スロットが空
        else if (itemstack7.isEmpty()) {
            if (slot5.mayPlace(itemstack2)) {
                slot5.setByPlayer(itemstack2);
                inventory.setItem(p_150432_, ItemStack.EMPTY);
            }
        }
        // ケース3: 両方にアイテムあり → スワップ
        else if (slot5.mayPickup(p_150434_) && slot5.mayPlace(itemstack2)) {
            inventory.setItem(p_150432_, itemstack7);  // ホットバーに移動
            slot5.setByPlayer(itemstack2);             // スロットに移動
            slot5.onTake(p_150434_, itemstack7);
        }
    }
}
```

### 重要パラメータ
- `p_150431_`: クリックされたスロットのインデックス
- `p_150432_`: 数字キーの番号（0-8）または 40（オフハンド）
- `p_150433_`: ClickType
- `p_150434_`: Player

### ロールスロットでの問題
ロールスロット（インデックス1-3）には `RoleSlotBarrier` アイテムが入っているため、SWAP操作時に：
1. バリアアイテムがホットバーに移動してしまう
2. または、バリアアイテムが邪魔でスワップできない

### 必要な処理
1. SWAP開始前: バリアアイテムを一時保存して除去
2. SWAP実行: 通常のスワップ処理
3. SWAP完了後: 元の位置にバリアアイテムを復元

## 実装方針

### Option 1: AbstractContainerMenu.clicked() への Mixin
- `@Inject(method = "clicked", at = @At("HEAD"), cancellable = true)`
- ロールスロットへのSWAPを検出したら、カスタム処理を実行
- メリット: シンプル、確実
- デメリット: clicked() の処理を完全に置き換える必要がある

### Option 2: Slot.getItem() / Slot.setByPlayer() への Mixin
- スロット取得/設定時にバリアアイテムを透過的に処理
- メリット: より汎用的
- デメリット: 複雑、副作用の可能性

### 推奨: Option 1
AbstractContainerMenu.clicked() の SWAP 処理部分だけをカスタマイズする方が明確で安全

## 次のステップ
1. AbstractContainerMenu.clicked() への Mixin を実装
2. ClickType.SWAP かつ slotId が 1-3 の場合を検出
3. バリアアイテムスワップ処理を実装
4. テスト
