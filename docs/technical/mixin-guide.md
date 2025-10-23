# Mixin実装ガイド

**いつ読むか**: Forgeイベントでは対応できない深いレベルでの動作変更が必要なとき

## Mixinとは

Mixinはバニラコードを直接改変するための強力なツール（SpongePowered提供）。Forgeイベントが存在しない箇所や、より深いレベルでの制御が必要な場合に使用する。

---

## いつMixinを使うべきか

### 適用場面

- ✅ Forgeイベントが存在しない処理
- ✅ 既存イベントでは不十分な制御が必要
- ✅ パフォーマンスが重要な箇所
- ✅ GUI操作の根本的変更

### 使うべきでない場面

- ❌ Forgeイベントで対応可能な場合
- ❌ パブリックAPIで実現できる場合
- ❌ 将来のバージョンアップで破壊的変更のリスクが高い箇所

---

## 基本的なMixin構造

```java
@Mixin(TargetClass.class)
public class TargetClassMixin {

    @Inject(
        method = "targetMethod",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onTargetMethod(
        // ターゲットメソッドと同じ引数
        ParameterType param1,
        ParameterType param2,
        // Mixin用のコールバック
        CallbackInfo ci
    ) {
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

---

## Mixin設定ファイル

### resources/one_slot_survival.mixins.json

```json
{
  "required": true,
  "package": "com.example.oneSlotSurvival.mixin",
  "compatibilityLevel": "JAVA_21",
  "refmap": "one_slot_survival.refmap.json",
  "mixins": [
    "AbstractContainerScreenMixin"
  ],
  "minVersion": "0.8"
}
```

**重要なフィールド:**
- `package`: Mixinクラスのパッケージ
- `mixins`: Mixinクラスのリスト（拡張子なし）
- `refmap`: リマッピング情報（Gradle自動生成）

### build.gradle設定

```gradle
minecraft {
    // ...
    runs {
        all {
            property 'mixin.env.remapRefMap', 'true'
            property 'mixin.env.refMapRemappingFile', "${projectDir}/build/createSrgToMcp/output.srg"
        }
    }
}

dependencies {
    // Mixin依存関係
    implementation 'org.spongepowered:mixin:0.8.5'
}
```

---

## 実装例

### 例1: スロットクリックの無効化

**目的:** 特定のスロットへのクリック操作を完全にブロックする

```java
package com.example.oneSlotSurvival.mixin;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClicked(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        // 条件判定
        if (OneSlotClientManager.isLocalPlayerRestricted() &&
            slot != null &&
            BarrierItem.shouldHaveBarrier(slot.getSlotIndex())) {

            // ユーザーフィードバック
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§c[One Slot] This slot is disabled"),
                true  // アクションバーに表示
            );

            // バニラ処理を完全にキャンセル
            ci.cancel();
        }
    }
}
```

**ポイント:**
- `@At("HEAD")`: メソッド開始時点で処理
- `cancellable = true`: `ci.cancel()` を使用可能に
- `CallbackInfo ci`: Mixinコールバック（必須）
- 条件分岐: 既存システム（OneSlotClientManager、BarrierItem）と連携

### 例2: 戻り値の変更

**目的:** メソッドの戻り値を変更する

```java
@Mixin(SomeClass.class)
public class SomeClassMixin {

    @Inject(method = "someMethod", at = @At("RETURN"), cancellable = true)
    private void modifyReturn(CallbackInfoReturnable<Integer> cir) {
        // 元の戻り値を取得
        int originalValue = cir.getReturnValue();

        // 条件に応じて変更
        if (shouldModify()) {
            cir.setReturnValue(originalValue * 2);
        }
    }
}
```

**ポイント:**
- `CallbackInfoReturnable<T>`: 戻り値のある場合
- `@At("RETURN")`: return文の直前
- `cir.getReturnValue()`: 元の戻り値取得
- `cir.setReturnValue(...)`: 戻り値変更

### 例3: フィールドアクセス

**目的:** privateフィールドにアクセスする

```java
@Mixin(TargetClass.class)
public abstract class TargetClassMixin {

    @Shadow
    private int somePrivateField;

    @Shadow
    protected abstract void somePrivateMethod();

    @Inject(method = "publicMethod", at = @At("HEAD"))
    private void onPublicMethod(CallbackInfo ci) {
        // privateフィールドにアクセス
        this.somePrivateField = 42;

        // privateメソッド呼び出し
        this.somePrivateMethod();
    }
}
```

**ポイント:**
- `@Shadow`: 既存フィールド・メソッドへのアクセス
- `abstract`: @Shadowメソッドはabstractで宣言

---

## @Atターゲットポイント

| ターゲット | 説明 | 使用例 |
|-----------|------|--------|
| `HEAD` | メソッド開始時 | 処理の前処理、キャンセル |
| `RETURN` | return文直前 | 戻り値の変更、後処理 |
| `TAIL` | メソッド終了時 | 後処理（戻り値変更不可） |
| `INVOKE` | 特定メソッド呼び出し前後 | 特定の呼び出しを置換 |

### INVOKE例

```java
@Inject(
    method = "someMethod",
    at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"
    ),
    cancellable = true
)
private void beforeHurt(CallbackInfo ci) {
    // Player.hurt()呼び出しの直前で処理
    ci.cancel();
}
```

---

## デバッグとトラブルシューティング

### Mixinが適用されない

**チェックリスト:**
1. `resources/one_slot_survival.mixins.json` が正しく配置されている
2. Mixinクラス名が設定ファイルに登録されている
3. `build.gradle` にMixin設定がある
4. `./gradlew clean build` で再ビルド

**ログ確認:**
```
[Mixin] Preparing mixins for mixin.one_slot_survival
[Mixin] Registering mixin: AbstractContainerScreenMixin
```

### コンパイルエラー

**よくあるエラー:**

```
@Shadow field type does not match target
```
→ フィールド型が一致していない。JAR抽出で正確な型を確認。

```
@Inject method signature does not match target
```
→ メソッドシグネチャが一致していない。引数の順序・型を確認。

```
@Mixin target class not found
```
→ ターゲットクラスが存在しない。import文とクラス名を確認。

### 実行時エラー

```
java.lang.NoSuchMethodError
```
→ メソッド名が間違っている。JAR抽出でメソッド名を確認。

```
MixinApplyError
```
→ Mixin適用に失敗。ログでエラー詳細を確認。

---

## ベストプラクティス

### 1. 最小限の改変

```java
// ❌ 悪い例: 広範囲にMixinを適用
@Inject(method = "*", at = @At("HEAD"))

// ✅ 良い例: 必要な箇所だけ
@Inject(method = "slotClicked", at = @At("HEAD"))
```

### 2. 条件分岐で制御

```java
// ✅ 良い例: 条件判定で影響範囲を限定
@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
private void onSlotClicked(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
    if (!shouldIntercept()) return;  // 早期リターン

    // カスタム処理
    ci.cancel();
}
```

### 3. ユーザーフィードバック

```java
// ✅ 良い例: 操作がブロックされたことを通知
Minecraft.getInstance().player.displayClientMessage(
    Component.literal("§c[One Slot] This slot is disabled"),
    true  // アクションバー表示
);
```

### 4. ドキュメント化

```java
/**
 * プレイヤーがバリアスロットをクリックした際に操作をブロックする。
 *
 * @reason One Slot Survivalの制限ルールに従い、特定スロットを無効化する
 * @author YourName
 */
@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
private void onSlotClicked(...) { ... }
```

---

## 実機テスト項目

Mixin実装後は必ず以下をテストする:

- [ ] 条件判定が正確に動作するか
- [ ] バニラ処理が完全にキャンセルされるか
- [ ] ユーザーフィードバックが表示されるか
- [ ] 他のMODとの互換性（可能な範囲）
- [ ] パフォーマンス影響（フレームレート）
- [ ] 意図しない副作用がないか

---

## 注意点

### 互換性リスク

- Mixinはバニラコードに依存するため、Minecraftバージョンアップで破壊される可能性がある
- 他のMODが同じメソッドにMixinを適用している場合、競合の可能性がある

### デバッグの困難さ

- Mixinのエラーは通常のJavaエラーとは異なる形で表示される
- スタックトレースが複雑になる

### パフォーマンス

- 過度のMixin使用はパフォーマンスに影響する可能性がある
- 頻繁に呼ばれるメソッドへのMixinは特に注意

---

## まとめ

**Mixinは強力だが慎重に使う:**
- ✅ Forgeイベントで対応できない場合の最終手段
- ✅ 最小限の改変に留める
- ✅ 適切なテストが必須
- ✅ ドキュメント化を怠らない

**成功例（このプロジェクト）:**
- AbstractContainerScreenMixin: スロットクリックの完全制御
- 条件分岐による影響範囲の限定
- ユーザーフィードバックの実装
- 既存システムとの連携
