# JAR抽出ガイド

**いつ読むか**: バニラMinecraftやForgeのソースコードを直接確認したいとき

## なぜJAR抽出が最優先なのか

- **正確性**: ドキュメントより正確、Web検索より信頼できる
- **最新性**: 使用中のバージョンに完全一致
- **速度**: 数秒でソースコード取得（Web検索より圧倒的に速い）
- **完全性**: すべてのメソッド、フィールド、コメントが見える

---

## 環境情報

**このプロジェクトはWindows環境で動作します。**

### パス指定

**Windowsパス形式を使用:**

```bash
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java
```

**ポイント:**
- Windowsパス形式 (`C:\...`) を使用
- バックスラッシュ区切り
- ダブルクォートで囲む

---

## JAR抽出の基本コマンド

### 単一ファイル抽出

```bash
jar -xf <JAR_PATH> <PACKAGE_PATH>
```

**例:**
```bash
jar -xf forge-sources.jar net/minecraft/world/level/saveddata/SavedData.java
```

### パッケージ全体抽出

```bash
jar -xf <JAR_PATH> <PACKAGE_DIR>/
```

**例:**
```bash
jar -xf forge-sources.jar net/minecraft/world/level/saveddata/
```

### JAR内容の探索

```bash
# ファイル一覧表示
jar -tf forge-sources.jar | grep SavedData

# 特定パターンで検索
jar -tf forge-sources.jar | grep "inventory.*Screen"
```

---

## 実践例

### 例1: AbstractContainerScreenの調査

```bash
# 1. 抽出
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java

# 2. 確認
ls net/minecraft/client/gui/screens/inventory/
# → AbstractContainerScreen.java
```

### 例2: SavedDataパッケージの全体調査

```bash
# パッケージ全体を抽出
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/world/level/saveddata/

# 内容確認
ls net/minecraft/world/level/saveddata/
# → SavedData.java, SavedDataType.java, DimensionDataStorage.java, ...
```

### 例3: 関連クラスの発見

```bash
# JAR内でインベントリ関連クラスを検索
jar -tf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" | grep "inventory.*Screen"

# 出力例:
# net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java
# net/minecraft/client/gui/screens/inventory/InventoryScreen.java
# net/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen.java

# 必要なファイルを抽出
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/client/gui/screens/inventory/InventoryScreen.java
```

---

## 抽出の利点

### 完全な実装の確認

```java
// 抽出前: ドキュメントや推測
public record SavedDataType<T> {
    // ???
}

// 抽出後: 完全な実装
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

## 抽出後のファイル管理

### 配置場所

抽出したファイルは作業ディレクトリ直下に展開されます:

```
one_slot_survival/
├── net/
│   └── minecraft/
│       └── client/
│           └── gui/
│               └── screens/
│                   └── inventory/
│                       └── AbstractContainerScreen.java
├── src/
├── docs/
└── ...
```

### クリーンアップ

```bash
# 抽出したファイルは .gitignore に既に登録されている
# 不要になったら削除可能（次回また抽出できる）
rm -rf net/
```

### .gitignore設定

```gitignore
# 既に設定済み
/net/
```

---

## トラブルシューティング

### エラー: jar: コマンドが見つかりません

**原因:** Java/JDKがインストールされていない、またはPATHが通っていない

**解決策:**
```bash
# Java確認
java -version

# JDK確認
javac -version

# Gradleを使用してJava確認
./gradlew --version
```

### エラー: ファイルが見つかりません

**原因:** Gradleキャッシュが存在しない

**解決策:**
```bash
# Gradle依存関係の再取得
./gradlew build

# キャッシュ場所の確認
ls "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge"
```

### 抽出したファイルが見つからない

**原因:** cd でディレクトリ移動した

**解決策:** 作業ディレクトリに戻る

```bash
# 作業ディレクトリに戻る
cd C:\Users\user\IdeaProjects\one_slot_survival

# 確認
ls net/minecraft/
```

---

## エージェント向けガイドライン

### 必ず守るべきルール

1. **Windowsパス形式を使用** - `C:\Users\godhe\...` 形式
2. **JAR抽出を最優先** - Web検索やTask toolの前に実行
3. **Readツールで確認** - 抽出後は必ずReadツールで内容確認

### やってはいけないこと

- ❌ cd でディレクトリ移動してから jar -xf を実行
- ❌ Web検索で古いソースコードを探す
- ❌ Task tool で general-purpose agent を起動（不要）

### 効率的なワークフロー

```
1. このドキュメント（source-extraction.md）を読む
   ↓
2. Windowsパス形式で jar -xf を実行
   ↓
3. Readツールで抽出したファイルを読む
   ↓
4. 必要に応じて関連クラスも抽出
   ↓
5. 分析結果をユーザーに報告
```

---

## まとめ

**JAR抽出の利点:**
- ✅ ドキュメントより正確
- ✅ 最新バージョンのAPIに対応
- ✅ Web検索より圧倒的に速い（数秒でソース取得）
- ✅ 隠れたAPIや変更点を発見できる
- ✅ 完全な実装を確認できる

**ベストプラクティス:**
1. まず環境を検出（pwd）
2. JAR抽出を最優先
3. Readツールで確認
4. 必要に応じて関連クラスも抽出
5. 不要になったらクリーンアップ
