# バニラコード調査ワークフロー

**いつ読むか**: バニラMinecraftの内部実装を理解したい、または改変対象のメソッドを特定したいとき

## ワークフロー概要

```
1. JAR抽出（最優先） → 正確なソースコード取得
   ↓
2. ダミー継承クラス作成（必要時） → 基本的なメソッドシグネチャ確認
   ↓
3. IDEナビゲーション → 使用例と継承関係を探索
   ↓
4. 実装と検証 → 実際に動くコードで理解を確認
```

---

## Phase 1: JAR抽出（最優先）

**このフェーズを最優先する理由:**
- 正確性: ドキュメントより正確、Web検索より信頼できる
- 速度: 数秒でソースコード取得
- 完全性: すべてのメソッド、フィールド、コメントが見える

### 基本手順

**1. JAR抽出（Windowsパス形式）**
```bash
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java
```

**2. Readツールで確認**
```
# Readツールで抽出したファイルを読む
net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java
```

**詳細:** [technical/source-extraction.md](../technical/source-extraction.md)

---

## Phase 2: ダミー継承クラス作成（必要時）

**いつ使うか:**
- JAR抽出だけでは不十分な場合
- メソッドシグネチャの確認が必要な場合
- IDEの自動補完を活用したい場合

### 「ダミー継承クラス」作戦

```java
// テストクラスを作成してコンパイル
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

### 段階的メソッド確認

```
1. 基本メソッド（mouseClicked）の確認
   ↓
2. コンパイル
   ↓
3. 専門メソッド（slotClicked）の確認
   ↓
4. 必要なimportを段階的に追加
```

---

## Phase 3: 「コンパイル駆動開発」

### なぜ有効か

- コンパイルエラーが**正確なシグネチャ**を教えてくれる
- ドキュメントより信頼できる
- 段階的に理解を深められる

### プロセス

```java
// 1. 試行（仮説実装）
data.field = tag.getInt("key");

// 2. コンパイルエラー
// 不適合な型: Optional<Integer>をintに変換できません

// 3. 発見（バニラAPIの変更点）
data.field = tag.getInt("key").orElse(0);

// 4. 理解の深化
```

### よくあるエラーパターン

**Phase 1: 型の不一致エラー**
```java
// 試行
data.field = tag.getInt("key");

// エラー
// 不適合な型: Optional<Integer>をintに変換できません

// 発見
data.field = tag.getInt("key").orElse(0);
```

**Phase 2: メソッド引数エラー**
```java
// 試行
new SavedDataType<>("id", supplier, codec);

// エラー
// SavedDataTypeに適切なコンストラクタが見つかりません
// 実引数リストと仮引数リストの長さが異なります

// 発見: 隠れた必須引数
new SavedDataType<>("id", supplier, codec, null);  // DataFixTypes必須
```

**Phase 3: シンボル解決エラー**
```java
// 試行
import net.minecraft.world.level.saveddata.SavedData.Factory;

// エラー
// シンボルを見つけられません: クラス Factory

// 発見: 存在しないクラス（ドキュメントの誤り）
import net.minecraft.world.level.saveddata.SavedDataType;  // 正しいクラス
```

---

## Phase 4: IDEナビゲーション活用

### クラス構造の完全理解

**継承階層の確認:**
```
Ctrl+H (IntelliJ): 継承ツリー表示
    ↓
親クラス・インターフェースのメソッドを確認
    ↓
オーバーライド可能メソッドを発見
```

**使用箇所の検索:**
```
Ctrl+Alt+F7 (Find Usages): バニラコードでの使用例を発見
    ↓
実際の使い方のパターンを学習
    ↓
パラメータの典型的な値を確認
```

**型階層の探索:**
```
Ctrl+クリック: 定義へジャンプ
Ctrl+Alt+Left: 戻る
    ↓
深い探索と高速な往復を両立
```

### 実例: DimensionDataStorage探索

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

---

## 実践例

### 例1: AbstractContainerScreenの調査

**目的:** slotClickedメソッドのシグネチャを知りたい

```bash
# 1. JAR抽出
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java

# 2. Readツールで確認
# → slotClickedメソッドを発見
```

**発見:**
```java
protected void slotClicked(Slot slot, int slotId, int mouseButton, ClickType type) {
    // 実装
}
```

### 例2: SavedDataTypeの完全理解

**目的:** コンストラクタの正確な引数を知りたい

```bash
# 1. JAR内でSavedData関連クラスを検索
jar -tf forge-sources.jar | grep SavedData

# 2. SavedDataTypeを抽出
jar -xf forge-sources.jar net/minecraft/world/level/saveddata/SavedDataType.java

# 3. Readツールで確認
```

**発見:**
```java
public record SavedDataType<T extends SavedData>(
    String id,
    Function<SavedData.Context, T> constructor,
    Function<SavedData.Context, Codec<T>> codec,
    DataFixTypes dataFixType  // ← ドキュメント未記載！
) {
    // オーバーロードも発見
    public SavedDataType(String id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType) {
        this(id, ctx -> constructor.get(), ctx -> codec, dataFixType);
    }
}
```

### 例3: 関連クラスの発見

**目的:** インベントリ関連のScreenクラスを網羅的に調査

```bash
# 1. JAR内で検索
jar -tf forge-sources.jar | grep "inventory.*Screen"

# 出力例:
# net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java
# net/minecraft/client/gui/screens/inventory/InventoryScreen.java
# net/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen.java

# 2. 必要なクラスを抽出
jar -xf forge-sources.jar net/minecraft/client/gui/screens/inventory/

# 3. すべてのファイルを確認
ls net/minecraft/client/gui/screens/inventory/
```

---

## 調査効率を最大化する戦略

### 1. ターゲットの絞り込み

```
広範な探索（クラス名検索）
  ↓
具体的な実装（メソッド確認）
  ↓
使用例の発見（Find Usages）
  ↓
完全理解（JAR抽出で全体像）
```

### 2. エラー駆動学習

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

### 3. 複数情報源の相互検証

```
公式ドキュメント（概要）
  ↓
コンパイルエラー（正確なシグネチャ）
  ↓
JAR抽出（完全な実装）
  ↓
実装成功（確実な理解）
```

---

## 抽出の利点

### 完全な実装の確認

```java
// JAR抽出前: ドキュメントや推測
public record SavedDataType<T> {
    // ???
}

// JAR抽出後: 完全な実装
public record SavedDataType<T extends SavedData>(
    String id,
    Function<SavedData.Context, T> constructor,
    Function<SavedData.Context, Codec<T>> codec,
    DataFixTypes dataFixType
) { ... }
```

### 隠れたAPIの発見

- publicだがドキュメント化されていないメソッド
- 便利なオーバーロード
- 内部実装の詳細（protected/privateメソッド）

### 継承関係の明確化

```java
// recordなのか、classなのか
public record SavedDataType<T> { ... }  // ← recordと判明

// 継承関係
public abstract class AbstractContainerScreen<T extends AbstractContainerMenu>
    extends AbstractScreen<T>
    implements MenuAccess<T> {
    // ...
}
```

---

## エージェント向けガイドライン

### 必ず守るべきルール

1. **JAR抽出を最優先する**
   - バニラコードの調査が必要になったら、**まずこのドキュメントを読む**
   - Web検索やTask toolを使う前に、**jar -xf コマンドで直接ソースを抽出する**

2. **Windowsパス形式を使用**
   - `C:\Users\godhe\...` 形式で指定

3. **効率的な調査順序**
   ```
   1. このドキュメント（vanilla-research.md）を読む
      ↓
   2. Windowsパス形式で jar -xf を実行
      ↓
   3. Readツールで抽出したファイルを読む
      ↓
   4. 関連クラスも必要なら追加で抽出
      ↓
   5. 分析結果をユーザーに報告
   ```

### やってはいけないこと

- ❌ Web検索で古いドキュメントを探す（時間の無駄）
- ❌ Task tool で general-purpose agent を起動する（JAR抽出で十分）
- ❌ cd でディレクトリ移動してから jar -xf を実行する

### 抽出後のクリーンアップ

```bash
# 抽出したファイルは .gitignore に既に登録されている
# 不要になったら削除可能（次回また抽出できる）
rm -rf net/
```

---

## まとめ

**効率的な調査フロー:**
1. **JAR抽出** - jar -xf コマンドで直接ソースを取得（最優先）
2. **Readツールで確認** - 完全な実装を確認
3. **ダミー継承クラス作成**（必要時） - 基本的なメソッドシグネチャを確認
4. **IDEナビゲーション** - 使用例と継承関係を探索
5. **実装と検証** - 実際に動くコードで理解を確認

**この手法の利点:**
- ✅ ドキュメントより正確
- ✅ 最新バージョンのAPIに対応
- ✅ 段階的に理解を深められる
- ✅ 隠れたAPIや変更点を発見できる
- ✅ Web検索より圧倒的に速い（数秒でソース取得）
