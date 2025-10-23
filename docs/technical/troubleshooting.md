# トラブルシューティングガイド

**いつ読むか**: エラーが解決できないとき、または問題解決の戦略を知りたいとき

## コンパイルエラー

### シンボルが見つかりません

```
error: cannot find symbol
  symbol:   class SavedDataFactory
  location: package net.minecraft.world.level.saveddata
```

**原因:**
- クラス名が間違っている
- パッケージが間違っている
- 存在しないクラス（ドキュメントの誤り）

**解決策:**
1. **JAR抽出で正確なクラス名を確認** (最優先)
   ```bash
   jar -tf forge-sources.jar | grep SavedData
   ```
2. import文を修正
3. 再コンパイル

**参考:** [technical/source-extraction.md](./source-extraction.md)

---

### 型の不一致

```
error: incompatible types: Optional<Integer> cannot be converted to int
    int value = tag.getInt("key");
```

**原因:**
- APIが変更されている（バージョン差異）
- Optionalなどのラッパー型が返される

**解決策:**
1. **JAR抽出で正確な戻り値型を確認**
   ```bash
   jar -xf forge-sources.jar net/minecraft/nbt/CompoundTag.java
   ```
2. 適切な型変換を追加
   ```java
   int value = tag.getInt("key").orElse(0);
   ```

---

### メソッド引数の数が違う

```
error: constructor SavedDataType in class SavedDataType cannot be applied to given types
  required: String,Function,Function,DataFixTypes
  found:    String,Function,Function
```

**原因:**
- 隠れた必須引数がある
- ドキュメントが古い

**解決策:**
1. **JAR抽出で完全なコンストラクタシグネチャを確認**
   ```bash
   jar -xf forge-sources.jar net/minecraft/world/level/saveddata/SavedDataType.java
   ```
2. 不足している引数を追加
   ```java
   new SavedDataType<>("id", constructor, codec, null);  // DataFixTypes追加
   ```

---

### メソッドが見つかりません

```
error: cannot find symbol
  symbol:   method setCanceled(boolean)
  location: variable event of type MouseScrollingEvent
```

**原因:**
- メソッドが存在しない
- バージョン違いでAPIが変更された

**解決策:**
1. **JAR抽出でクラス定義を確認**
2. 利用可能なメソッドを確認
3. **代替アプローチを検討** (例: Mixin使用)

---

## 実行時エラー

### NullPointerException

```
java.lang.NullPointerException: Cannot invoke "..." because "..." is null
```

**原因:**
- ライフサイクル問題（初期化前にアクセス）
- クライアント/サーバー問題（サーバー側でクライアント専用APIを使用）
- ワールドロード前にデータアクセス

**解決策:**

**1. ライフサイクルを確認**
```java
// ❌ 悪い例: 静的初期化でアクセス
public static final Player PLAYER = Minecraft.getInstance().player;  // null!

// ✅ 良い例: 使用時に取得
public static Player getPlayer() {
    return Minecraft.getInstance().player;  // 使用時に取得
}
```

**2. Null チェック追加**
```java
Player player = Minecraft.getInstance().player;
if (player != null) {
    // 処理
}
```

**3. @OnlyIn アノテーションを確認**
```java
// クライアント専用コード
@OnlyIn(Dist.CLIENT)
public void clientOnlyMethod() {
    // ...
}
```

---

### ClassNotFoundException / NoSuchMethodError

```
java.lang.ClassNotFoundException: com.example.oneSlotSurvival.mixin.AbstractContainerScreenMixin
java.lang.NoSuchMethodError: 'void net.minecraft.world.entity.player.Player.someMethod()'
```

**原因:**
- ビルド設定の問題
- Mixin設定の問題
- クラスパスの問題

**解決策:**

**1. クリーンビルド**
```bash
./gradlew clean build
```

**2. Mixin設定確認**
```json
// resources/one_slot_survival.mixins.json
{
  "required": true,
  "package": "com.example.oneSlotSurvival.mixin",
  "mixins": [
    "AbstractContainerScreenMixin"  // 正しいクラス名
  ]
}
```

**3. JAR抽出でメソッド存在を確認**
```bash
jar -xf forge-sources.jar net/minecraft/world/entity/player/Player.java
```

---

### MixinApplyError

```
org.spongepowered.asm.mixin.injection.throwables.InjectionError:
  Critical injection failure: @Inject annotation on onSlotClicked could not be applied
```

**原因:**
- Mixinターゲットメソッドが見つからない
- メソッドシグネチャが一致しない
- @At ターゲットポイントが無効

**解決策:**

**1. メソッド名を確認**
```java
// ❌ 間違ったメソッド名
@Inject(method = "slotClick", ...)  // slotClick ではない

// ✅ 正しいメソッド名
@Inject(method = "slotClicked", ...)
```

**2. メソッドシグネチャを確認**
```bash
# JAR抽出でシグネチャ確認
jar -xf forge-sources.jar net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java
```

**3. Mixinログを確認**
```
[Mixin] Preparing mixins for mixin.one_slot_survival
[Mixin] ERROR: Mixin apply failed AbstractContainerScreenMixin -> ...
```

**参考:** [technical/mixin-guide.md](./mixin-guide.md)

---

## Forgeイベント問題

### イベントが発火しない

```java
@SubscribeEvent
public void onEvent(SomeEvent event) {
    // 呼ばれない
}
```

**チェックリスト:**

**1. イベント登録を確認**
```java
// ❌ 登録忘れ
public class EventHandler {
    @SubscribeEvent
    public void onEvent(SomeEvent event) { ... }
}

// ✅ 正しい登録
@Mod.EventBusSubscriber(modid = "one_slot_survival")
public class EventHandler {
    @SubscribeEvent
    public void onEvent(SomeEvent event) { ... }
}
```

**2. Side を確認**
```java
// クライアント専用イベント
@Mod.EventBusSubscriber(modid = "one_slot_survival", value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public void onScreenEvent(ScreenEvent event) { ... }
}
```

**3. イベントバスを確認**
```java
// Forge Event Bus (ほとんどのイベント)
@Mod.EventBusSubscriber(modid = "one_slot_survival")

// Mod Event Bus (ライフサイクルイベント)
@Mod.EventBusSubscriber(modid = "one_slot_survival", bus = Mod.EventBusSubscriber.Bus.MOD)
```

**4. イベントログで確認**
```java
@SubscribeEvent
public void onEvent(SomeEvent event) {
    System.out.println("Event fired: " + event.getClass().getName());
    // イベントが発火しているかログで確認
}
```

---

### イベントキャンセルできない

```java
@SubscribeEvent
public void onEvent(SomeEvent event) {
    event.setCanceled(true);  // メソッドが見つからない
}
```

**原因:**
- イベントがキャンセル可能でない
- Forgeイベントの制限

**解決策:**

**1. キャンセル可能か確認**
```bash
# JAR抽出でイベントクラスを確認
jar -tf forge-sources.jar | grep ScreenEvent
jar -xf forge-sources.jar net/minecraftforge/client/event/ScreenEvent.java

# @Cancelable アノテーションがあるか確認
```

**2. 代替アプローチ: Mixin使用**
```java
// Forgeイベントでキャンセルできない場合、Mixinを使用
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {
    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClicked(..., CallbackInfo ci) {
        ci.cancel();  // Mixinなら確実にキャンセル可能
    }
}
```

**参考:** [technical/mixin-guide.md](./mixin-guide.md)

---

## ビルド問題

### Gradleビルドが失敗する

```
FAILURE: Build failed with an exception.
```

**チェックリスト:**

**1. Java バージョン確認**
```bash
java -version
# Java 21 が必要

./gradlew --version
# Gradle 8.x が必要
```

**2. 依存関係の再取得**
```bash
./gradlew clean
./gradlew --refresh-dependencies build
```

**3. Gradle キャッシュのクリア**
```bash
rm -rf ~/.gradle/caches/
./gradlew build
```

**4. IDE再同期**
- IntelliJ IDEA: File → Invalidate Caches / Restart
- VSCode: Gradle Reload

---

### Mixin設定エラー

```
[Mixin] ERROR: Mixin config one_slot_survival.mixins.json does not specify "minVersion" property
```

**解決策:**
```json
// resources/one_slot_survival.mixins.json
{
  "required": true,
  "package": "com.example.oneSlotSurvival.mixin",
  "compatibilityLevel": "JAVA_21",
  "refmap": "one_slot_survival.refmap.json",
  "mixins": [
    "AbstractContainerScreenMixin"
  ],
  "minVersion": "0.8"  // ← 追加
}
```

---

## Web検索戦略

### 効果的な検索キーワード

**段階的絞り込み:**
```
段階1: "Minecraft Forge 1.21 inventory slot click cancel"
段階2: "ScreenEvent.MouseButtonPressed.Pre cancel method"
段階3: "AbstractContainerScreen slotClicked method signature"
```

**バージョン指定:**
```
"Minecraft Forge 1.21.8 SavedData usage 2025"
"NeoForge 1.21 event handling"  # Forge 1.20.2+ は NeoForge と類似
```

**情報源の優先順位:**
1. 公式Forgeドキュメント（参考程度）
2. NeoForgedドキュメント（API類似）
3. Forgeフォーラム（実際の問題解決例）
4. GitHub Issues（同様の問題）

**参考:** [research-troubleshooting.md](../research-troubleshooting.md)（旧ドキュメント）

---

### Forge GitHub活用

**Issues検索:**
```
site:github.com/MinecraftForge/MinecraftForge "1.21" "ScreenEvent" "cancel"
```

**Commit履歴:**
- API変更点の確認
- 推奨実装方法の学習

**PR（Pull Request）:**
- 新機能の実装例
- 修正パターンの学習

---

## 問題解決フローチャート

```
問題発生
    ↓
コンパイルエラー？
    YES → JAR抽出で正確なAPIを確認 → 修正
    NO  ↓
実行時エラー？
    YES → スタックトレース確認 → ライフサイクル/Null問題を調査
    NO  ↓
イベントが動作しない？
    YES → 登録方法・Side・イベントバスを確認
    NO  ↓
Mixin問題？
    YES → Mixinログ確認 → シグネチャ・設定を確認
    NO  ↓
ビルド問題？
    YES → クリーンビルド → 依存関係再取得
    NO  ↓
Web検索 → GitHub Issues → ユーザーに質問
```

---

## エージェント向けガイドライン

### 困ったときの行動

1. **このドキュメント（troubleshooting.md）を読む**
2. **該当する解決策を試す**
3. **JAR抽出で正確な情報を確認**（最優先）
4. **Web検索で類似事例を探す**
5. **ユーザーに質問する**（情報不足の場合）

### やってはいけないこと

- ❌ 同じエラーを繰り返す（ドキュメント未確認）
- ❌ Web検索だけで解決しようとする（JAR抽出が最優先）
- ❌ 長時間試行錯誤する（適切なガイドを読めば解決する）
- ❌ エラーメッセージを無視する（重要な情報が含まれている）

### 効率的なトラブルシューティング

```
1. エラーメッセージを確認
   ↓
2. このドキュメントで該当するセクションを探す
   ↓
3. 解決策を試す
   ↓
4. JAR抽出で正確な情報を確認（必要時）
   ↓
5. 解決できない場合はユーザーに報告
```

---

## まとめ

**トラブルシューティングの鉄則:**
- ✅ エラーメッセージを丁寧に読む
- ✅ JAR抽出で正確な情報を確認（最優先）
- ✅ ログを活用する
- ✅ 段階的にデバッグする
- ✅ 解決策をドキュメント化する

**よくある問題パターン:**
- コンパイルエラー → JAR抽出で解決
- NullPointerException → ライフサイクル問題
- イベント問題 → 登録方法・Side確認
- Mixin問題 → シグネチャ・設定確認
