# オフハンドからロールスロットへの移動制限（Fキー制限）

## 目的

ゲーム中にオフハンドからロールスロットへの移動（Fキー）を制限する。
つるはしはロールスロット専用アイテムなので、インベントリ画面から適切に配置すべき。

## バニラのFキー処理フロー

### クライアント側
1. `Minecraft.java:1939` - `keySwapOffhand` キー入力検知
2. `Minecraft.java:1941-1944` - パケット送信
   - `ServerboundPlayerActionPacket(SWAP_ITEM_WITH_OFFHAND, ...)`
   - **クライアント側でアイテムを直接入れ替えない**（サーバーからの同期を待つ）

### サーバー側
3. `ServerGamePacketListenerImpl.java:1169-1178` - パケット処理
   - `ForgeEventFactory.onLivingSwapHandItems(this.player)` 呼び出し（1171行目）
   - Forgeイベント `LivingSwapItemsEvent.Hands` 発火
   - イベントがキャンセルされた場合、`null` が返される（1172行目）
   - `null` の場合は `return` で処理終了（入れ替えなし）
   - キャンセルされなかった場合のみ入れ替え実行（1173-1174行目）:
     ```java
     this.player.setItemInHand(InteractionHand.OFF_HAND, event.getItemSwappedToOffHand());
     this.player.setItemInHand(InteractionHand.MAIN_HAND, event.getItemSwappedToMainHand());
     ```

## Forgeイベント詳細

### `LivingSwapItemsEvent.Hands`
- **パッケージ**: `net.minecraftforge.event.entity.living.LivingSwapItemsEvent.Hands`
- **特性**: キャンセル可能（`Cancellable`）
- **発火タイミング**: `ServerGamePacketListenerImpl.handlePlayerAction`
- **発火場所**: サーバー側のみ

### イベントメソッド
- `getItemSwappedToMainHand()` - メインハンドに移動される予定のアイテム（現在のオフハンド）
- `getItemSwappedToOffHand()` - オフハンドに移動される予定のアイテム（現在のメインハンド）
- `setItemSwappedToMainHand(ItemStack)` - メインハンドに移動するアイテムを変更
- `setItemSwappedToOffHand(ItemStack)` - オフハンドに移動するアイテムを変更
- `setCanceled(true)` - イベントをキャンセル（入れ替えを防止）

## 実装方針

### 採用する方式
**Forgeイベント `LivingSwapItemsEvent.Hands` をインターセプト**

### 実装箇所
`ServerEvent.java` に新しいイベントハンドラメソッド追加

### 制限ロジック
```java
@SubscribeEvent
public static void onLivingSwapHandItems(LivingSwapItemsEvent.Hands event) {
    // プレイヤーのみ対象
    if (!(event.getEntity() instanceof ServerPlayer player)) {
        return;
    }

    // MOD有効なプレイヤーのみ対象
    if (!PlayerModValidity.isEffective(player)) {
        return;
    }

    // オフハンド → メインハンドへ移動される予定のアイテム
    ItemStack offhandItem = event.getItemSwappedToMainHand();

    // つるはしの場合、ロールスロット専用なので直接移動を禁止
    if (!offhandItem.isEmpty() && RoleSlot.isPickaxe(offhandItem)) {
        event.setCanceled(true);
    }
}
```

## 懸念点: マルチプレイでの同期

### プレイヤーインスタンスの存在パターン

#### Integrated Server（シングルプレイ）
- サーバースレッド: `ServerPlayer` × 1
- クライアントスレッド: `LocalPlayer` × 1

#### Dedicated Server（マルチプレイ、プレイヤー1人の場合）
- **サーバー側**: `ServerPlayer` × 1
- **クライアント側**: `ServerPlayer` × 1 + `LocalPlayer` × 1

### 懸念内容

`LivingSwapItemsEvent.Hands` は `ServerGamePacketListenerImpl` で発火するため：
- **サーバー側の `ServerPlayer`**: イベント発火 → キャンセル可能 ✓
- **クライアント側の `ServerPlayer`**: イベント発火しない（パケットリスナーがクライアント側にないため）❓

**問題**: クライアント側の `ServerPlayer` でイベントキャンセルが適用されない可能性がある。

### 予想される動作

バニラの実装を見ると：
1. クライアント側はパケット送信のみで、**ローカルでアイテムを入れ替えない**
2. サーバー側でイベントがキャンセルされると、`setItemInHand` が実行されない
3. サーバー側でアイテムが変更されないため、クライアントへの同期パケットも送られない（はず）
4. クライアント側は同期されないため、状態が変わらない

**結論**: 理論上は両方のワールドで正しく動作するはずだが、**実際のテストで確認が必要**。

## テスト項目

### Integrated Server（シングルプレイ）
- [ ] つるはしをオフハンドに持つ → Fキー → 入れ替わらないことを確認
- [ ] 通常アイテムをオフハンドに持つ → Fキー → 通常通り入れ替わることを確認

### Dedicated Server（マルチプレイ）
- [ ] つるはしをオフハンドに持つ → Fキー → 入れ替わらないことを確認
- [ ] 通常アイテムをオフハンドに持つ → Fキー → 通常通り入れ替わることを確認
- [ ] クライアント・サーバー両方で状態が一致していることを確認

## 参考ファイル

- `net/minecraft/client/Minecraft.java:1939-1944` - クライアント側のFキー処理
- `net/minecraft/server/network/ServerGamePacketListenerImpl.java:1169-1178` - サーバー側のパケット処理
- `net/minecraftforge/event/entity/living/LivingSwapItemsEvent.java` - Forgeイベント定義
- `net/minecraftforge/event/ForgeEventFactory.java:891-894` - イベントファクトリ実装
