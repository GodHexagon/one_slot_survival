# Forge API調査ガイド

**いつ読むか**: Forge APIの使い方を調査したいとき、公式ドキュメントが古い・不完全なとき

## 調査フロー

```
Web検索（概要把握） → テストクラス作成（実験） → コンパイルエラー（正確なAPI発見） → JAR抽出（完全理解） → 実装完成
```

---

## Phase 1: Web検索

**検索戦略:**
```
"Minecraft Forge 1.21 [API名] usage tutorial 2025"
"Forge [API名] [関連クラス] implementation"
"Minecraft 1.21.8 Forge [API名] example"
```

**重要な情報源:**
1. 公式Forgeドキュメント（参考程度・古い可能性）
2. NeoForgedドキュメント（APIが類似）
3. Forgeフォーラム（実際の問題解決例）
4. GitHub Examples

**注意:** 公式ドキュメントは最新APIと一致しない場合が多い

---

## Phase 2: コンパイル駆動開発

**なぜ有効か:**
- コンパイルエラーが **正確なシグネチャ** を教えてくれる
- ドキュメントより信頼できる
- 段階的に理解を深められる

**基本手順:**
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

**よくあるエラーパターン:**
- 型の不一致 → 正しい型を発見
- メソッド引数の数が違う → 隠れた必須引数を発見
- シンボルが見つからない → 正しいパッケージ・クラスを発見

---

## Phase 3: JAR抽出で完全理解

詳細は [バニラコード調査](./vanilla-code-research.md) を参照。

**基本:**
```bash
# 環境検出
pwd

# JAR抽出（環境に応じたパス）
jar -xf "path/to/forge-sources.jar" net/minecraft/world/level/saveddata/SavedDataType.java

# Read ツールで確認
```

**発見例（SavedDataType）:**
```java
public record SavedDataType<T extends SavedData>(
    String id,
    Function<SavedData.Context, T> constructor,
    Function<SavedData.Context, Codec<T>> codec,
    DataFixTypes dataFixType  // ← ドキュメントに記載なし！
) { ... }
```

---

## Phase 4: 実装例（SavedData）

```java
public class TestSavedData extends SavedData {
    private int exampleCounter;

    public TestSavedData() { this(0); }
    public TestSavedData(int counter) { this.exampleCounter = counter; }

    // SavedDataType定義（Forge 1.21.8）
    public static final SavedDataType<TestSavedData> TYPE = new SavedDataType<>(
        "test_saved_data",
        TestSavedData::new,
        Codec.INT.xmap(TestSavedData::new, data -> data.exampleCounter),
        null  // DataFixTypes（カスタムデータはnull）
    );

    public static TestSavedData getForLevel(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void incrementCounter() {
        this.exampleCounter++;
        this.setDirty();  // 保存トリガー
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

## まとめ

**Forge API調査の黄金律:**
1. 公式ドキュメントを鵜呑みにしない
2. コンパイルエラーは最良の教師
3. ソースコードは嘘をつかない
4. 段階的に理解を深める
