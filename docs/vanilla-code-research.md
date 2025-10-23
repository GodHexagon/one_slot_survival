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

**⚠️ 重要: 環境に応じたパス指定**

**このプロジェクトはリモートリポジトリを使用しており、Windows/WSL/Linux環境で動作する可能性があります。**

**ステップ1: 環境を自動検出**
```bash
# 現在の環境を確認
pwd
# Windows (Git Bash): /c/Users/godhe/git/one_slot_survival
# WSL/Linux:          /mnt/c/Users/godhe/git/one_slot_survival または /home/username/...

# Gradleキャッシュの場所を確認
ls ~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/ 2>/dev/null || ls /c/Users/godhe/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/ 2>/dev/null
```

**ステップ2: 環境別のパス指定方法**

**【Windows Git Bash環境の場合】** (`pwd` が `/c/Users/...` を返す場合)
```bash
# ✅ Windowsパスをそのまま使用
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen.java
```

**【WSL/Linux環境の場合】** (`pwd` が `/mnt/c/...` または `/home/...` を返す場合)
```bash
# ✅ UNIXパスを使用
jar -xf "/mnt/c/Users/godhe/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.21.8-58.1.0_mapped_official_1.21.8/forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen.java

# または ~/.gradle を使用（Linuxネイティブの場合）
jar -xf ~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.21.8-58.1.0_mapped_official_1.21.8/forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar net/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen.java
```

**ステップ3: 汎用的なGradleキャッシュ検索**
```bash
# 環境に依存しない検索方法
find ~/.gradle/caches/forge_gradle/minecraft_user_repo -name "*sources.jar" 2>/dev/null | grep "1.21.8-58.1.0"

# または、プロジェクトのGradleビルドから参照
./gradlew dependencies --configuration compileClasspath | grep forge
```

**抽出の実例（環境検出付き）:**
```bash
# 1. 環境検出
if [ -d "/c/Users/godhe/.gradle" ]; then
    # Windows Git Bash
    GRADLE_CACHE="C:\Users\godhe\.gradle"
    JAR_PATH="$GRADLE_CACHE\\caches\\forge_gradle\\minecraft_user_repo\\net\\minecraftforge\\forge\\1.21.8-58.1.0_mapped_official_1.21.8\\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar"
elif [ -d "/mnt/c/Users/godhe/.gradle" ]; then
    # WSL
    JAR_PATH="/mnt/c/Users/godhe/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.21.8-58.1.0_mapped_official_1.21.8/forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar"
else
    # Linux (ホームディレクトリ)
    JAR_PATH=~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.21.8-58.1.0_mapped_official_1.21.8/forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar
fi

# 2. 抽出実行
jar -xf "$JAR_PATH" net/minecraft/client/gui/screens/inventory/AbstractRecipeBookScreen.java
```

**簡易版（エージェント向け）:**
```bash
# まず、現在の環境でpwdを実行して環境を判定
# その結果に応じて適切なパス形式を使用する

# Windows Git Bashの場合 (pwd → /c/Users/...)
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\...\sources.jar" net/minecraft/...

# WSL/Linuxの場合 (pwd → /mnt/c/... または /home/...)
jar -xf "/mnt/c/Users/godhe/.gradle/caches/forge_gradle/.../sources.jar" net/minecraft/...
# または
jar -xf ~/.gradle/caches/forge_gradle/.../sources.jar net/minecraft/...
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

### Claude Code エージェントへの指示

**🤖 エージェントが必ず守るべきルール:**

**1. JAR抽出を最優先する**
- バニラコードの調査が必要になったら、**まずこのドキュメントを読む**
- Web検索やTask toolを使う前に、**jar -xf コマンドで直接ソースを抽出する**
- このプロジェクトでは、Gradleキャッシュに完全なソースコードが既に存在している

**2. 環境を必ず検出する**
- このプロジェクトは **Windows/WSL/Linux環境で動作する** （リモートリポジトリ使用）
- **必ず最初に `pwd` を実行**して環境を判定する
- 判定結果に応じて適切なパス形式を使用：
  - `/c/Users/...` → Windows Git Bash → `C:\Users\...` 形式
  - `/mnt/c/Users/...` → WSL → `/mnt/c/Users/...` 形式
  - `/home/...` → Linux → `~/.gradle/...` 形式

**3. 効率的な調査順序**
```
1. このドキュメント（vanilla-code-research.md）を読む
   ↓
2. pwd を実行して環境を検出
   ↓
3. 環境に応じたパス形式で jar -xf を実行
   ↓
4. Read ツールで抽出したファイルを読む
   ↓
5. 関連クラスも必要なら追加で抽出
   ↓
6. 分析結果をユーザーに報告
```

**4. やってはいけないこと**
- ❌ Web検索で古いドキュメントを探す（時間の無駄）
- ❌ Task tool で general-purpose agent を起動する（JAR抽出で十分）
- ❌ 環境検出をせずに決め打ちでパスを指定する
- ❌ cd でディレクトリ移動してから jar -xf を実行する（ワーキングディレクトリが変わる）

**5. 抽出後のクリーンアップ**
```bash
# 抽出したファイルは .gitignore に既に登録されている
# 不要になったら削除しても良い（次回また抽出できる）
rm -rf net/
```

### まとめ: バニラコード調査のベストプラクティス

**効率的な調査フロー:**
1. **このドキュメントを読む** - 環境固有の注意事項を確認
2. **JAR抽出** - jar -xf コマンドで直接ソースを取得（最優先）
3. **ダミー継承クラス作成** - 必要に応じて基本的なメソッドシグネチャを確認
4. **IDEナビゲーション** - 使用例と継承関係を探索
5. **実装と検証** - 実際に動くコードで理解を確認

**この手法の利点:**
- ✅ ドキュメントより正確
- ✅ 最新バージョンのAPIに対応
- ✅ 段階的に理解を深められる
- ✅ 隠れたAPIや変更点を発見できる
- ✅ Web検索より圧倒的に速い（数秒でソース取得）