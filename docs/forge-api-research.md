# Forge API調査ガイド

**いつ読むか**: Forge APIの使い方を調査したいとき、公式ドキュメントが古い・不完全なとき

このガイドでは、**SavedData API（Forge 1.21.8）** を例に、実際の調査プロセスを解説します。

---

## 調査の全体フロー

```
1. Web検索で公式ドキュメント・最新情報を収集
   ↓
2. 「コンパイル駆動開発」でテストクラスを作成
   ↓
3. コンパイルエラーから正しいAPIシグネチャを発見
   ↓
4. JARファイルからソースコードを直接抽出して確認
   ↓
5. 実装を完成させて動作確認
```

---

## Phase 1: Web検索による情報収集

### 検索戦略

**段階的キーワード絞り込み**
```
段階1: "Minecraft Forge 1.21 SavedData usage tutorial 2025"
段階2: "Forge SavedData Level ServerLevel persistent data"
段階3: "Minecraft 1.21.8 Forge saveddata example implementation"
```

### 重要な情報源

1. **公式Forgeドキュメント** - `https://docs.minecraftforge.net/en/latest/datastorage/saveddata/`
2. **NeoForgedドキュメント** - APIが類似しているため参考になる
3. **Forgeフォーラム** - 実際の問題解決例
4. **GitHub Examples** - チュートリアルリポジトリ

### Web検索での発見（SavedData例）

**公式ドキュメントの情報:**
```java
// ドキュメントに記載されていた例（古いAPI）
DimensionDataStorage storage = level.getDataStorage();
storage.computeIfAbsent(this::load, this::create, "example");
```

**NeoForgedドキュメントからの示唆:**
- 最新版では **Codecベース** のシリアライゼーション
- `SavedDataType` という新しいクラスの存在
- 古いNBTベースの `load/save` メソッドから変更されている

**重要な気づき:** 公式ドキュメントは最新APIと一致しない場合がある！

---

## Phase 2: コンパイル駆動開発（CDD）

### なぜコンパイル駆動開発なのか

- IDEがForge/Minecraftソースコードへのアクセスを提供
- コンパイルエラーが **正確なシグネチャ** を教えてくれる
- ドキュメントより信頼できる

### 実践：テストクラスの作成

**ステップ1: 初期テストクラス作成**
```java
package com.github.godhexagon.oneslotsurvival.world.data;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class TestSavedData extends SavedData {
    private int exampleCounter = 0;

    public TestSavedData() {
        super();
    }

    // 公式ドキュメント通りに実装してみる
    public static TestSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        TestSavedData data = new TestSavedData();
        data.exampleCounter = tag.getInt("counter");  // ← エラーが出る
        return data;
    }
}
```

**ステップ2: コンパイルして発見**
```bash
./gradlew classes
```

**エラーメッセージから学ぶ:**
```
エラー: 不適合な型: Optional<Integer>をintに変換できません:
    data.exampleCounter = tag.getInt("counter");
```

**発見1:** `tag.getInt()` は `Optional<Integer>` を返す（バニラMinecraftの変更）

**修正:**
```java
data.exampleCounter = tag.getInt("counter").orElse(0);
```

---

### SavedDataType の探索

**試行1: ドキュメント通りに実装**
```java
public static final SavedDataType<TestSavedData> TYPE = new SavedDataType<>(
    TestSavedData::create,
    TestSavedData::load,
    "test_saved_data"
);
```

**コンパイルエラー:**
```
エラー: シンボルを見つけられません
  シンボル:   クラス Factory
  場所: クラス SavedData
```

**発見2:** `SavedData.Factory` は存在しない

---

**試行2: 別パッケージを探す**
```java
import net.minecraft.world.level.saveddata.SavedDataType;  // ✅ 成功

DimensionDataStorage storage = level.getDataStorage();
return storage.computeIfAbsent(TYPE);
```

**コンパイルエラー:**
```
エラー: クラス DimensionDataStorageのメソッド computeIfAbsentは指定された型に適用できません。
  期待値: SavedDataType<T>
  検出値:    TestSavedData::load,TestSavedData::create,String
```

**発見3:** `computeIfAbsent()` は **`SavedDataType<T>` 単一パラメータ** を取る（ドキュメントと異なる！）

---

**試行3: Codecベースで実装**
```java
public static final SavedDataType<TestSavedData> TYPE = new SavedDataType<>(
    "test_saved_data",
    TestSavedData::new,
    RecordCodecBuilder.create(instance -> instance.group(
        Codec.INT.fieldOf("counter").forGetter((TestSavedData sd) -> sd.exampleCounter),
        Codec.STRING.fieldOf("data").forGetter((TestSavedData sd) -> sd.exampleData)
    ).apply(instance, TestSavedData::new))
);
```

**コンパイルエラー:**
```
エラー: SavedDataTypeに適切なコンストラクタが見つかりません(String,Supplier<T>,Codec<T>)
    コンストラクタ SavedDataType(..., DataFixTypes)は使用できません
      (実引数リストと仮引数リストの長さが異なります)
```

**発見4:** 第4引数に **`DataFixTypes`** が必要！

---

## Phase 3: ソースコード直接確認

### JARからソースを抽出

コンパイルエラーだけでは判明しない詳細を、実際のソースコードから確認します。

**Forgeソースコードの場所:**
```
.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/
{version}_mapped_official_{mcversion}/
forge-{version}_mapped_official_{mcversion}-sources.jar
```

**ソース抽出コマンド:**
```bash
jar -xf "path/to/forge-sources.jar" net/minecraft/world/level/saveddata/SavedDataType.java
```

**実際のソースコード確認:**
```java
// SavedDataType.java (Forge 1.21.8)
public record SavedDataType<T extends SavedData>(
    String id,
    Function<SavedData.Context, T> constructor,
    Function<SavedData.Context, Codec<T>> codec,
    DataFixTypes dataFixType
) {
    // 便利なオーバーロード
    public SavedDataType(String id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType) {
        this(id, ctx -> constructor.get(), ctx -> codec, dataFixType);
    }
}
```

**重要な発見:**
1. `record` 型で定義されている
2. 第4引数 `DataFixTypes` が必須
3. 便利なオーバーロードが存在（`Supplier<T>` と `Codec<T>` を直接渡せる）

---

### DataFixTypes の調査

```bash
jar -xf "path/to/forge-sources.jar" net/minecraft/util/datafix/DataFixTypes.java
```

**DataFixTypes.java確認結果:**
```java
public enum DataFixTypes {
    LEVEL(References.LEVEL),
    PLAYER(References.PLAYER),
    SAVED_DATA_SCOREBOARD(References.SAVED_DATA_SCOREBOARD),
    // ... バニラのSavedData用の型が定義されている
}
```

**結論:** カスタムMODのSavedDataには **`null`** を渡せば良い

---

## Phase 4: 完成実装

### 最終的な実装

**TestSavedData.java**
```java
package com.github.godhexagon.oneslotsurvival.world.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class TestSavedData extends SavedData {

    private int exampleCounter;
    private String exampleData;

    // デフォルトコンストラクタ
    public TestSavedData() {
        this(0, "");
    }

    // Codec用コンストラクタ
    public TestSavedData(int counter, String data) {
        this.exampleCounter = counter;
        this.exampleData = data;
    }

    // SavedDataType定義（Forge 1.21.8 API）
    public static final SavedDataType<TestSavedData> TYPE = new SavedDataType<>(
        "test_saved_data",           // .datファイルのID
        TestSavedData::new,           // 新規作成用Supplier
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("counter").forGetter((TestSavedData sd) -> sd.exampleCounter),
            Codec.STRING.fieldOf("data").forGetter((TestSavedData sd) -> sd.exampleData)
        ).apply(instance, TestSavedData::new)),  // Codec
        null                          // DataFixTypes（カスタムデータはnull）
    );

    // アクセスメソッド
    public static TestSavedData getForLevel(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    // ビジネスロジック
    public void incrementCounter() {
        this.exampleCounter++;
        this.setDirty();  // 保存をトリガー
    }

    public int getCounter() {
        return this.exampleCounter;
    }

    public void setExampleData(String data) {
        this.exampleData = data;
        this.setDirty();  // 保存をトリガー
    }

    public String getExampleData() {
        return this.exampleData;
    }
}
```

### 使用例：コマンド実装

**TestSavedDataCommand.java**
```java
public class TestSavedDataCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("testsaveddata")
                .then(Commands.literal("increment")
                    .executes(context -> {
                        ServerLevel level = context.getSource().getLevel();
                        TestSavedData data = TestSavedData.getForLevel(level);
                        data.incrementCounter();
                        context.getSource().sendSuccess(
                            () -> Component.literal("Counter: " + data.getCounter()),
                            true
                        );
                        return data.getCounter();
                    })
                )
        );
    }
}
```

---

## 調査で得られた重要な知見

### Forge 1.21.8 SavedData API の真実

| 項目 | 公式ドキュメント | 実際のAPI (1.21.8) |
|------|-----------------|-------------------|
| シリアライゼーション | NBTベース | **Codecベース** |
| `computeIfAbsent` 引数 | `(load, create, name)` | **`SavedDataType<T>`** |
| `SavedDataType` コンストラクタ | 3引数 | **4引数 (+DataFixTypes)** |
| `save()` メソッド | 必須実装 | **不要（Codecが自動処理）** |
| `setDirty()` | 変更時に呼ぶ | **必須（変わらず）** |

### API変更の背景

**Codecベースへの移行理由:**
- データバージョン管理の自動化
- 型安全性の向上
- JSONとの相互運用性
- 将来のマイグレーション対応

---

## 調査テクニックのまとめ

### 1. Web検索での工夫

- **最新年を含める**: "2025", "1.21.8" などで検索
- **複数バージョン比較**: NeoForge, Fabric含めて調査
- **フォーラムを活用**: "[SOLVED]" タグのスレッド優先

### 2. コンパイル駆動開発（CDD）の利点

✅ **正確性**: ドキュメントより信頼できる
✅ **効率性**: 試行錯誤で段階的に理解
✅ **最新性**: 実際に使用中のバージョンで確認

### 3. ソースコード直接確認

**IDEの活用:**
- **Ctrl+N (Cmd+O)**: クラス検索
- **Ctrl+クリック**: 定義ジャンプ
- **Ctrl+H**: 継承階層表示

**JAR抽出コマンド:**
```bash
# 特定ファイル抽出
jar -xf forge-sources.jar path/to/File.java

# JAR内容リスト確認
jar -tf forge-sources.jar | grep SavedData
```

### 4. 調査の優先順位

```
1. プロジェクトのForge/Minecraftソース（最優先・最も正確）
   ↓
2. コンパイルエラーメッセージ（シグネチャが判明）
   ↓
3. Web上の最新情報（フォーラム・GitHub）
   ↓
4. 公式ドキュメント（参考程度・古い可能性）
```

---

## トラブルシューティング

### よくある問題と解決策

**問題1: 「型推論できません」エラー**
```java
// ❌ エラー
.forGetter(sd -> sd.field)

// ✅ 解決策：明示的に型を指定
.forGetter((TestSavedData sd) -> sd.field)
```

**問題2: 「実引数と仮引数の長さが異なる」**
```java
// ❌ 引数不足
new SavedDataType<>("id", supplier, codec)

// ✅ DataFixTypes追加
new SavedDataType<>("id", supplier, codec, null)
```

**問題3: 「save()メソッドがオーバーライドできない」**
```java
// ❌ 古いAPI
@Override
public CompoundTag save(CompoundTag tag) { ... }

// ✅ Codecで自動処理（save()メソッド不要）
// RecordCodecBuilder でフィールドを定義するだけ
```

---

## 他のAPIへの応用

この調査手法は **任意のForge API** に適用できます：

**例：新しいイベントシステムの調査**
1. Web検索: "Forge 1.21 ScreenEvent usage"
2. テストクラス作成: `ScreenEvent.MouseButtonPressed.Pre` を実装
3. コンパイルエラー確認: `setCanceled()` が使えない？
4. ソース確認: `jar -xf forge-sources.jar net/minecraftforge/client/event/ScreenEvent.java`
5. 代替手段発見: Mixin使用に切り替え

**例：新しいレジストリシステムの調査**
1. Web検索: "Forge 1.21 DeferredRegister"
2. 既存コード確認: `ModAttributes.java` を参照
3. テスト実装: 新しい `Attribute` を登録
4. コンパイルで確認: シグネチャと使い方を学習

---

## 結論

### Forge API調査の黄金律

1. **公式ドキュメントを鵜呑みにしない**
2. **コンパイルエラーは最良の教師**
3. **ソースコードは嘘をつかない**
4. **段階的に理解を深める**

### 最も効率的な調査フロー

```
Web検索（概要把握）
  ↓
テストクラス作成（実験）
  ↓
コンパイルエラー（正確なAPI発見）
  ↓
ソース確認（完全な理解）
  ↓
実装完成
```

この手法を使えば、**ドキュメントが古い・不完全でも、正確なAPIを発見できる**！
