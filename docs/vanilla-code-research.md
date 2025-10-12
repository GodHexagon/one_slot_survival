## バニラコード調査・解析

**いつ読むか**: バニラMinecraftの内部実装を理解したい、または改変対象のメソッドを特定したいとき

### ターゲットメソッドの特定

**「ダミー継承クラス」作戦**
```java
// 実際に使った手法：AbstractContainerScreenを継承したテストクラス
public class TestAbstractContainerScreen extends AbstractContainerScreen<AbstractContainerMenu> {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // IDEが自動補完で正確な署名を表示 ✅
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
        // パラメータ名・型が全て判明 ✅
        super.slotClicked(slot, slotId, mouseButton, type);
    }
}
```

**段階的メソッド確認**
- 基本メソッド（mouseClicked）→ 専門メソッド（slotClicked）の順で調査
- 各段階でコンパイルして、利用可能なメソッドを確認
- 必要なimportを段階的に追加

### 利用可能ツール

- **MinecraftDecompiler**: MC 1.21 JARファイルの自動デコンパイル
- **McDeob**: 公式Mojang Mappingsを使用した高速リマッピング（約6秒）
- **公式Mojang Mappings**: MC 1.21で利用可能、NeoForge 1.20.2+では標準

### 開発環境での確認

- 既存の開発環境にForge 1.21が設定済みの場合、IDEで直接クラスを確認可能
- テストクラス作成→コンパイル→署名確認のサイクル

### 「コンパイル駆動開発」

- テストクラス作成 → コンパイルエラー → 署名判明
- エラーメッセージから正しいAPIを推測
- IDEの自動補完を最大限活用

### JARからのソースコード直接抽出

**いつ使うか**: IDEでの確認だけでは不十分な場合、またはクラス全体の構造を理解したいとき

**Gradleキャッシュからの抽出:**
```bash
# Forge/Minecraftソースの場所
.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/
{version}_mapped_official_{mcversion}/
forge-{version}_mapped_official_{mcversion}-sources.jar

# ソース抽出コマンド例
jar -xf "path/to/forge-sources.jar" net/minecraft/world/level/saveddata/SavedDataType.java

# 抽出後、直接ファイルを読む
# → プロジェクトルートに net/ ディレクトリが作成される
```

**JAR内容の探索:**
```bash
# ファイル一覧表示（関連クラスを探す）
jar -tf forge-sources.jar | grep SavedData

# 特定パッケージ配下をすべて抽出
jar -xf forge-sources.jar net/minecraft/world/level/saveddata/
```

**抽出の利点:**
- **完全な実装**: コンストラクタ、全メソッド、フィールド、コメントが見える
- **継承関係**: `record`, `extends`, `implements` が明確
- **オーバーロード発見**: 複数のコンストラクタやメソッドシグネチャを一度に確認
- **隠れたAPI**: publicだがドキュメント化されていないメソッドを発見

**実例: SavedDataType調査での発見:**
```java
// 抽出したソースから発見したこと
public record SavedDataType<T extends SavedData>(
    String id,
    Function<SavedData.Context, T> constructor,
    Function<SavedData.Context, Codec<T>> codec,
    DataFixTypes dataFixType  // ← ドキュメントに記載なし！
) {
    // 便利なオーバーロードを発見
    public SavedDataType(String id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType) {
        this(id, ctx -> constructor.get(), ctx -> codec, dataFixType);
    }
}
```

### コンパイルエラーからのAPI学習プロセス

**段階的エラー解決による理解深化:**

**Phase 1: 型の不一致エラー**
```java
// 試行
data.field = tag.getInt("key");

// エラー
不適合な型: Optional<Integer>をintに変換できません

// 発見: バニラAPIの変更点
data.field = tag.getInt("key").orElse(0);
```

**Phase 2: メソッド引数エラー**
```java
// 試行
new SavedDataType<>("id", supplier, codec);

// エラー
SavedDataTypeに適切なコンストラクタが見つかりません
実引数リストと仮引数リストの長さが異なります

// 発見: 隠れた必須引数
new SavedDataType<>("id", supplier, codec, null);  // DataFixTypes必須
```

**Phase 3: シンボル解決エラー**
```java
// 試行
import net.minecraft.world.level.saveddata.SavedData.Factory;

// エラー
シンボルを見つけられません: クラス Factory

// 発見: 存在しないクラス（ドキュメントの誤り）
import net.minecraft.world.level.saveddata.SavedDataType;  // 正しいクラス
```

### IDEナビゲーション活用

**クラス構造の完全理解:**

**継承階層の確認:**
- **Ctrl+H**: 継承ツリー表示
- 親クラス・インターフェースのメソッドを確認
- オーバーライド可能メソッドを発見

**使用箇所の検索:**
- **Ctrl+Alt+F7 (Find Usages)**: バニラコードでの使用例を発見
- 実際の使い方のパターンを学習
- パラメータの典型的な値を確認

**型階層の探索:**
- **Ctrl+クリック**: 定義へジャンプ
- **Ctrl+Alt+Left**: 戻る
- 深い探索と高速な往復を両立

**実例: DimensionDataStorage探索**
```java
// 1. computeIfAbsentの定義を確認
public <T extends SavedData> T computeIfAbsent(SavedDataType<T> type) {
    // 実装を確認 → SavedDataTypeが必要と判明
}

// 2. 使用例を検索（Find Usages）
// → バニラのScoreboardSavedDataなどで使用例を発見

// 3. SavedDataTypeのコンストラクタを確認
// → 必要な引数が明確化
```

### 調査効率を最大化する戦略

**1. ターゲットの絞り込み**
```
広範な探索（クラス名検索）
  ↓
具体的な実装（メソッド確認）
  ↓
使用例の発見（Find Usages）
  ↓
完全理解（JAR抽出で全体像）
```

**2. エラー駆動学習**
```
仮説実装
  ↓
コンパイルエラー
  ↓
エラーメッセージ解析
  ↓
修正・再コンパイル
  ↓
理解の深化
```

**3. 複数情報源の相互検証**
```
公式ドキュメント（概要）
  ↓
コンパイルエラー（正確なシグネチャ）
  ↓
JAR抽出（完全な実装）
  ↓
実装成功（確実な理解）
```

### まとめ: バニラコード調査のベストプラクティス

**効率的な調査フロー:**
1. **ダミー継承クラス作成** - 基本的なメソッドシグネチャを確認
2. **コンパイル駆動開発** - エラーから正確なAPIを学習
3. **JAR抽出** - 複雑な場合は完全なソースを確認
4. **IDEナビゲーション** - 使用例と継承関係を探索
5. **実装と検証** - 実際に動くコードで理解を確認

**この手法の利点:**
- ✅ ドキュメントより正確
- ✅ 最新バージョンのAPIに対応
- ✅ 段階的に理解を深められる
- ✅ 隠れたAPIや変更点を発見できる