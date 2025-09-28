# フェーズ3: クライアントサイド変更実装計画

## 概要
カスタムパケット不使用のクライアント側推測方式を採用し、段階的にUI制限を実装する。

## 基本方針

### クライアント側状態推測
- **バリアアイテム検出**: `slot_barrier`アイテムが禁止スロットに存在する → プレイヤーが制限対象
- **パケット不要**: サーバーからの専用通信は行わない
- **リアルタイム判定**: 毎フレーム/必要時にバリアアイテムをチェック

### 既存アイテムの役割明確化
- **slot_barrier**: 現在の用途（禁止スロットブロック）専用
- **新アイテム**: 必要に応じて新規ID割り当て（例：ui_indicator）

## 段階的実装計画

### フェーズ3.1: クライアント状態判定システム
**目標**: クライアント側でプレイヤーの制限状態を正確に判定

**実装内容**:
```java
// OneSlotClientManager.java (新規作成)
@OnlyIn(Dist.CLIENT)
public class OneSlotClientManager {
    public static boolean isLocalPlayerRestricted() {
        // バリアアイテムの存在をチェック
        // ホットバー1-8 + インベントリ9-35にslot_barrierがある → 制限中
    }
}
```

**実装難易度**: ★☆☆
**リスク**: 低
**テスト項目**:
- 制限有効時にバリアアイテムが検出される
- 制限無効時に検出されない

### フェーズ3.2: ホットバースクロール無効化
**目標**: 制限対象プレイヤーのマウスホイールによるホットバー変更を無効化

**実装内容**:
```java
// OneSlotClientEvents.java (新規作成)
@SubscribeEvent
public static void onClientTick(TickEvent.ClientTickEvent event) {
    if (OneSlotClientManager.isLocalPlayerRestricted()) {
        // ホットバー選択を0に固定
        Minecraft.getInstance().player.getInventory().selected = 0;
    }
}
```

**実装難易度**: ★★☆
**リスク**: 中（入力処理の競合可能性）
**テスト項目**:
- マウスホイールでスロット変更されない
- 数字キー1-9でスロット変更されない
- スロット0以外選択できない

### フェーズ3.3: インベントリスロット視覚変更
**目標**: 禁止スロットを視覚的に区別（暗くする/アイコン表示）

**実装内容**:
```java
// InventoryScreenMixin.java (Mixin使用)
@Mixin(InventoryScreen.class)
public class InventoryScreenMixin {
    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void onRenderSlot(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (OneSlotClientManager.isLocalPlayerRestricted() && isRestrictedSlot(slot)) {
            // スロットを暗くレンダリング
        }
    }
}
```

**実装難易度**: ★★★
**リスク**: 高（Mixinの複雑さ、レンダリング競合）
**テスト項目**:
- 禁止スロットが視覚的に区別される
- アイテムは見える状態を維持
- 他のMODとの互換性

### フェーズ3.4: スロットクリック無効化
**目標**: 禁止スロットへのクリック操作を無効化

**実装内容**:
```java
@SubscribeEvent
public static void onGuiScreenEvent(ScreenEvent.MouseButtonPressed event) {
    if (event.getScreen() instanceof InventoryScreen &&
        OneSlotClientManager.isLocalPlayerRestricted()) {
        // 禁止スロットクリックをキャンセル
    }
}
```

**実装難易度**: ★★☆
**リスク**: 中（UI操作の競合）

## 技術的考慮事項

### バリアアイテム制限
- **現在のslot_barrier**: 禁止スロットブロック専用
- **互換性**: 既存の実装を変更しない
- **拡張性**: 必要に応じて新アイテムID追加

### クライアント側判定の精度
- **利点**: パケット不要、シンプル
- **制限**: バリアアイテム同期遅延の可能性
- **対策**: 定期的なチェック、キャッシュ機能

### Forge API使用方針
- **@OnlyIn(Dist.CLIENT)**: クライアント専用コード
- **ClientTickEvent**: 定期処理
- **ScreenEvent**: GUI操作制御
- **Mixin**: 最小限の使用（フェーズ3.3のみ）

## 実装優先度

**必須（フェーズ3.1-3.2）**:
- 基本的なUI制限機能
- プレイヤー体験の改善

**任意（フェーズ3.3-3.4）**:
- 視覚的な改善
- より洗練されたUX

この計画により、段階的にクライアントサイド機能を追加し、問題発生時は前のフェーズに戻ることができます。