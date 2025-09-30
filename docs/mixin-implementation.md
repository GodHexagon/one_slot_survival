## Mixin実装

**いつ読むか**: Forgeイベントでは対応できない深いレベルでの動作変更が必要なとき

### なぜMixinを使うのか

Forgeイベントでは対応できない深いレベルでの動作変更が必要な場合、Mixinを使用してバニラコードを直接改変する。

**適用場面**
- Forgeイベントが存在しない処理
- 既存イベントでは不十分な制御が必要
- パフォーマンスが重要な箇所
- GUI操作の根本的変更

### 基本的なMixin構造

```java
@Mixin(TargetClass.class)
public class TargetClassMixin {

    @Inject(method = "targetMethod", at = @At("HEAD"), cancellable = true)
    private void onTargetMethod(Parameters parameters, CallbackInfo ci) {
        // 条件判定
        if (shouldIntercept()) {
            // カスタム処理
            handleCustomLogic();

            // バニラ処理をキャンセル
            ci.cancel();
        }
    }
}
```

### 成功例: AbstractContainerScreenMixin

```java
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClicked(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        if (OneSlotClientManager.isLocalPlayerRestricted() &&
            slot != null &&
            BarrierItem.shouldHaveBarrier(slot.getSlotIndex())) {

            // フィードバック
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§c[One Slot] This slot is disabled"), true
            );

            // 完全キャンセル
            ci.cancel();
        }
    }
}
```

### ベストプラクティス

**技術的要点**
- `@At("HEAD")`: メソッド開始時点での処理
- `cancellable = true`: CallbackInfoによるキャンセル可能
- `ci.cancel()`: バニラ処理の完全バイパス
- 条件判定: 既存システムとの連携

**注意点**
- Mixinは強力だが互換性リスクあり
- 最小限の改変に留める
- 適切なテストが必須
- ドキュメント化を怠らない

### 実機テスト項目

- 条件判定の正確性
- バニラ処理の完全キャンセル
- ユーザーフィードバックの表示
- 他MODとの互換性