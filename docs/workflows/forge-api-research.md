# Forge API調査ワークフロー

**いつ読むか**: Forge APIの使い方を調査したいとき、公式ドキュメントが古い・不完全なとき

## ワークフロー概要

```
1. Web検索（概要把握）
   ↓
2. テストクラス作成（実験）
   ↓
3. コンパイルエラー（正確なAPI発見）
   ↓
4. JAR抽出（完全理解）
   ↓
5. 実装完成
```

---

## Phase 1: Web検索（概要把握）

### 検索戦略

**検索キーワードパターン:**
```
"Minecraft Forge 1.21 [API名] usage tutorial 2025"
"Forge [API名] [関連クラス] implementation"
"Minecraft 1.21.8 Forge [API名] example"
```

**バージョン指定:**
```
"Minecraft Forge 1.21.8 SavedData usage 2025"
"NeoForge 1.21 event handling"  # Forge 1.20.2+ は NeoForge と類似
```

### 重要な情報源

**優先順位:**
1. **公式Forgeドキュメント**（参考程度・古い可能性）
2. **NeoForgedドキュメント**（APIが類似）
3. **Forgeフォーラム**（実際の問題解決例）
4. **GitHub Examples**

**注意:** 公式ドキュメントは最新APIと一致しない場合が多い

### Forge GitHub活用

**Issues検索:**
```
site:github.com/MinecraftForge/MinecraftForge "1.21" "SavedData" "usage"
```

**Commit履歴:**
- API変更点の確認
- 推奨実装方法の学習

**PR（Pull Request）:**
- 新機能の実装例
- 修正パターンの学習

---

## Phase 2: コンパイル駆動開発

### なぜ有効か

- コンパイルエラーが **正確なシグネチャ** を教えてくれる
- ドキュメントより信頼できる
- 段階的に理解を深められる

### 基本手順

```java
// 1. テストクラスを作成
public class TestAPI extends SomeClass {
    // ドキュメントや推測に基づいて実装
}

// 2. コンパイル
./gradlew classes

// 3. エラーから学ぶ
// 不適合な型: Optional<Integer>をintに変換できません
// → tag.getInt("key").orElse(0) に修正

// 4. 繰り返す
```

### よくあるエラーパターン

**型の不一致:**
```java
// 試行
data.field = tag.getInt("key");

// エラー
// 不適合な型: Optional<Integer>をintに変換できません

// 解決
data.field = tag.getInt("key").orElse(0);
```

**メソッド引数の数が違う:**
```java
// 試行
new SavedDataType<>("id", supplier, codec);

// エラー
// SavedDataTypeに適切なコンストラクタが見つかりません

// 解決: 隠れた必須引数を発見
new SavedDataType<>("id", supplier, codec, null);  // DataFixTypes必須
```

**シンボルが見つからない:**
```java
// 試行
import net.minecraft.world.level.saveddata.SavedData.Factory;

// エラー
// シンボルを見つけられません: クラス Factory

// 解決: 正しいクラスを発見
import net.minecraft.world.level.saveddata.SavedDataType;
```

---

## Phase 3: JAR抽出で完全理解

**いつ使うか:**
- コンパイルエラーだけでは不十分な場合
- クラス全体の構造を理解したい場合
- 隠れたAPIを発見したい場合

### 基本手順

```bash
# 1. 環境検出
pwd

# 2. JAR抽出（環境に応じたパス）
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/world/level/saveddata/SavedDataType.java

# 3. Readツールで確認
```

**詳細:** [technical/source-extraction.md](../technical/source-extraction.md)

### 発見例（SavedDataType）

```java
// JAR抽出で発見
public record SavedDataType<T extends SavedData>(
    String id,
    Function<SavedData.Context, T> constructor,
    Function<SavedData.Context, Codec<T>> codec,
    DataFixTypes dataFixType  // ← ドキュメントに記載なし！
) {
    // 便利なオーバーロードも発見
    public SavedDataType(String id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType) {
        this(id, ctx -> constructor.get(), ctx -> codec, dataFixType);
    }
}
```

---

## Phase 4: 実装例

### SavedData実装（Forge 1.21.8）

```java
public class TestSavedData extends SavedData {
    private int exampleCounter;

    // コンストラクタ
    public TestSavedData() {
        this(0);
    }

    public TestSavedData(int counter) {
        this.exampleCounter = counter;
    }

    // SavedDataType定義（Forge 1.21.8）
    public static final SavedDataType<TestSavedData> TYPE = new SavedDataType<>(
        "test_saved_data",                                    // ID
        TestSavedData::new,                                   // コンストラクタ
        Codec.INT.xmap(TestSavedData::new, data -> data.exampleCounter),  // Codec
        null                                                  // DataFixTypes（カスタムデータはnull）
    );

    // ワールドから取得
    public static TestSavedData getForLevel(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    // データ操作
    public void incrementCounter() {
        this.exampleCounter++;
        this.setDirty();  // 保存トリガー
    }
}
```

### Forgeイベント実装

```java
@Mod.EventBusSubscriber(modid = "one_slot_survival", value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onScreenOpen(ScreenEvent.Opening event) {
        // スクリーンを開く前の処理
        if (event.getScreen() instanceof InventoryScreen) {
            // インベントリ画面を開いた時の処理
        }
    }
}
```

---

## 重要な発見（SavedData API）

| 項目 | 公式ドキュメント | 実際のAPI (1.21.8) |
|------|-----------------|-------------------|
| シリアライゼーション | NBTベース | **Codecベース** |
| `computeIfAbsent` 引数 | `(load, create, name)` | **`SavedDataType<T>`** |
| `SavedDataType` コンストラクタ | 3引数 | **4引数 (+DataFixTypes)** |
| `save()` メソッド | 必須実装 | **不要（Codec自動処理）** |

---

## Forge API階層の理解

```
バニラMinecraft → Forge拡張 → ModAPI の層構造
├─ Vanilla: AbstractContainerScreen#slotClicked
├─ Forge: ScreenEvent.MouseButtonPressed.Pre
└─ Mod: カスタムMixin実装
```

**理解のポイント:**
- Forgeイベントは「バニラコードへのフック」
- すべてのバニラ処理に対してイベントがあるわけではない
- イベントで対応できない場合はMixinを検討

---

## イベントシステムの使い分け

### Forge Event Bus

**ほとんどのゲームイベント:**
```java
@Mod.EventBusSubscriber(modid = "one_slot_survival")
public class EventHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) { ... }
}
```

### Mod Event Bus

**ライフサイクルイベント:**
```java
@Mod.EventBusSubscriber(modid = "one_slot_survival", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) { ... }
}
```

### Side指定

**クライアント専用イベント:**
```java
@Mod.EventBusSubscriber(modid = "one_slot_survival", value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onScreenEvent(ScreenEvent event) { ... }
}
```

---

## トラブルシューティング

### イベントが発火しない

**チェックリスト:**
1. `@Mod.EventBusSubscriber` アノテーションを確認
2. `modid` が正しいか確認
3. `Side` が適切か確認（CLIENT/SERVER）
4. イベントバス（Forge/Mod）が正しいか確認

**参考:** [technical/troubleshooting.md](../technical/troubleshooting.md)

### イベントキャンセルできない

**原因:**
- イベントが`@Cancelable`でない
- Forgeイベントの制限

**解決策:**
- Mixin使用を検討

**参考:** [technical/mixin-guide.md](../technical/mixin-guide.md)

### コンパイルエラーが解決できない

**解決策:**
1. **JAR抽出で正確なAPIを確認**（最優先）
2. コンパイルエラーメッセージを丁寧に読む
3. 必要に応じてWeb検索

**参考:** [technical/troubleshooting.md](../technical/troubleshooting.md)

---

## Forgeバージョン間差異

### イベントシステムの変遷

```java
// 古い方式 (Forge 1.12.x)
@SubscribeEvent
public void onGuiOpen(GuiOpenEvent event) { ... }

// 現代的方式 (Forge 1.21.x)
@SubscribeEvent
public void onScreenOpen(ScreenEvent.Opening event) { ... }
```

### API変更への対応

**1.19.x → 1.21.x:**
- 破壊的変更が多い
- パッケージ構造の変更
- クラス名の変更

**NeoForge分岐（2024年以降）:**
- Forge 1.20.2+ から分岐
- APIは類似しているが一部異なる

**マッピング変更:**
- MCP → 公式Mojang Mappings
- クラス名・メソッド名が変更

---

## エージェント向けガイドライン

### 必ず守るべきルール

1. **Web検索は概要把握のみ**
   - ドキュメントを鵜呑みにしない
   - 最新情報の確認を優先

2. **コンパイルエラーを活用**
   - エラーメッセージは最良の教師
   - 段階的に理解を深める

3. **JAR抽出で完全理解**
   - コンパイルエラーだけでは不十分な場合
   - ソースコードは嘘をつかない

4. **効率的な調査順序**
   ```
   1. このドキュメント（forge-api-research.md）を読む
      ↓
   2. Web検索で概要把握（参考程度）
      ↓
   3. テストクラス作成 → コンパイル → エラー解析
      ↓
   4. JAR抽出で完全理解（必要時）
      ↓
   5. 実装完成
   ```

### やってはいけないこと

- ❌ 公式ドキュメントを鵜呑みにする
- ❌ Web検索だけで解決しようとする
- ❌ コンパイルエラーを無視する
- ❌ 長時間試行錯誤する（適切なガイドを読めば解決する）

---

## まとめ

**Forge API調査の黄金律:**
1. 公式ドキュメントを鵜呑みにしない
2. コンパイルエラーは最良の教師
3. ソースコードは嘘をつかない
4. 段階的に理解を深める

**効率的な調査フロー:**
```
Web検索（概要） → テストクラス作成（実験） → コンパイル（正確なAPI発見）
→ JAR抽出（完全理解） → 実装完成
```

**重要な発見:**
- Forge APIは頻繁に変更される
- ドキュメントは最新APIと一致しない場合が多い
- JAR抽出が最も信頼できる情報源
