# バニラクラス使用箇所の網羅的調査手法

**いつ読むか**: バニラMinecraftのクラス（GameType、Abilities、Inventoryなど）がどこでどのように使われているか網羅的に調査したいとき

**目的**: 特定のバニラクラスの使用箇所をすべて特定し、機能別に分類することで、MOD実装の参考資料を作成する

---

## 調査の全体フロー

```
1. Web検索で基本情報収集
   ↓
2. 旧バージョンJavadocで使用パターン把握
   ↓
3. （オプション）ダミーテストクラスでメソッド確認
   ↓
4. Agent toolでデコンパイルソース全検索
   ↓
5. 機能別分類とドキュメント化
```

---

## ステップ1: Web検索で基本情報収集

### 目的
- クラスの基本構造（enum、interface、classなど）を理解
- 主要なメソッドを把握
- 使用パターンの概要をつかむ

### 具体的な手順

**検索クエリ例**:
```
Minecraft Forge 1.21 [クラス名] usage examples methods
Minecraft net.minecraft.[パッケージ].[クラス名] class reference 1.21
```

**GameTypeの例**:
```
Minecraft Forge 1.21 GameType usage examples methods
Minecraft net.minecraft.world.level.GameType class reference 1.21
```

**確認すべき情報**:
- クラスの型（enum、interface、abstract class、concrete classなど）
- パッケージ名
- 主要なメソッドのシグネチャ
- よく使われるパターン

### 実績

GameTypeの調査では以下が判明：
- enum型で4つの定数（SURVIVAL、CREATIVE、ADVENTURE、SPECTATOR）
- `getId()`, `getName()`, `updatePlayerAbilities()`などの主要メソッド
- Forge 1.16.5のJavadocが詳細な情報源として有用

---

## ステップ2: 旧バージョンJavadocで使用パターン把握

### 目的
- 最新バージョンのドキュメントが不完全な場合、旧バージョンで補完
- "Uses of Class"ページで使用箇所の概要を把握
- クライアント/サーバー分離などのアーキテクチャを理解

### 具体的な手順

**利用可能なJavadoc**:
- **Forge Javadoc (1.16.5など)**: `https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.16.5/`
- **Yarn mappings**: `https://maven.fabricmc.net/docs/yarn-[version]+build.[number]/`

**"Uses of Class"ページを確認**:
```
https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.16.5/net/minecraft/world/class-use/GameType.html
```

このページには：
- クラス別の使用箇所
- メソッド別の使用箇所
- フィールド別の使用箇所

が整理されている。

### WebFetchツールの活用

```
WebFetch tool:
URL: [Javadocページ]
Prompt: "Extract all classes and methods that use [クラス名], organizing them by: 1) the class name, 2) the method signature, 3) the purpose/context of the usage"
```

### 実績

GameTypeの調査では：
- **クライアント側**: PlayerController、NetworkPlayerInfo、CreateWorldScreen など
- **サーバー側**: PlayerInteractionManager、PlayerList、ServerPlayerEntity など
- 計10個の主要使用パターンを特定

---

## ステップ3: （オプション）ダミーテストクラスでメソッド確認

### 目的
- IDEの自動補完機能を使って、正確なメソッドシグネチャを確認
- コンパイルエラーから正しいAPIを推測

### 具体的な手順

**1. テストディレクトリ作成**:
```bash
mkdir -p src/test/java/com/github/godhexagon/oneslotsurvival/test
```

**2. ダミークラス作成**:
```java
package com.github.godhexagon.oneslotsurvival.test;

import net.minecraft.world.level.GameType;
import net.minecraft.server.level.ServerPlayer;

/**
 * Test class to explore [クラス名] usage in Minecraft 1.21
 * This class is for research purposes only
 */
public class [クラス名]UsageTest {

    public void explore[クラス名]Methods() {
        // Get all values (for enums)
        GameType survival = GameType.SURVIVAL;

        // Explore methods (IDE will show available methods)
        // - getId()
        // - getName()
        // etc.
    }

    public void exploreRelatedClasses(ServerPlayer player) {
        // Check how the class is used with other classes
        // player.gameMode (likely a ServerPlayerGameMode object)
    }
}
```

**3. コンパイル**:
```bash
./gradlew compileTestJava
```

**4. IDEで確認**:
- コンパイル成功後、IDEでクラスを開く
- メソッド呼び出しで自動補完を使って利用可能なメソッドを確認
- 必要に応じてsuper.method()を呼んで、継承元のメソッドを確認

### 注意点
- このクラスは調査用なので、後で削除してもよい
- コンパイルが目的なので、実装は不要
- vanilla-code-research.mdの「ダミー継承クラス」作戦と組み合わせると効果的

---

## ステップ4: Agent toolでデコンパイルソース全検索

### 目的
- Forge 1.21.8の**実際のソースコード**で使用箇所を網羅的に検索
- 機能別に分類
- 30以上の詳細な分類でも対応

### 具体的な手順

**1. デコンパイルソースの場所を確認**:
```bash
ls C:/Users/godhe/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/
```

出力例：
```
1.21.8-58.1.0_mapped_official_1.21.8
```

**2. Agent toolを起動**:

```
Task tool (subagent_type: general-purpose)

prompt:
## タスク概要
Minecraft Forge 1.21.8のデコンパイルソースコードで `[クラス名]` が使用されている箇所をすべて検索し、機能別に分類してください。

## 調査対象ディレクトリ
`C:/Users/godhe/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.21.8-58.1.0_mapped_official_1.21.8/`

## 実行手順
1. まず `[クラス名]` クラス自体の定義を読んで、どのようなクラスか、どんなメソッドがあるかを理解する
2. `[クラス名]` が使用されているすべてのファイルを検索（import文、変数宣言、メソッド引数、戻り値など）
3. 各使用箇所のコードを読み、以下の情報を収集：
   - クラス名とパッケージ
   - メソッド名とシグネチャ
   - [クラス名]がどのように使われているか（取得/設定/判定など）
   - その機能の目的（例：プレイヤーの能力制御、ブロック破壊判定、UI表示など）

4. 収集した情報を機能別に分類：
   - 一つの機能を実装するために一か所だけ使用され、それらがたくさんある場合、それぞれを別の分類とする
   - 結果的に30以上の分類になっても構わない
   - 網羅的に分類することが重要

## 期待する出力形式
以下のマークダウン形式で報告してください：

```markdown
# [クラス名]使用箇所の機能別分類（Minecraft Forge 1.21.8）

## [クラス名] クラスの基本情報
- パッケージ: xxx
- 型: enum / interface / class
- 主要メソッド: method1(), method2(), ...

## 機能分類

### 1. [機能名]
- **目的**: [この機能が何をするか]
- **使用箇所**:
  - `パッケージ.クラス名.メソッド名(引数)` - [詳細な説明]
  - ...

### 2. [機能名]
...

## 統計
- 総使用箇所数: XX箇所
- 機能分類数: XX個
- 主要な使用パターン: [パターンのサマリー]
```

## 注意事項
- **網羅性を最優先**：見落としがないよう、すべての使用箇所を確認
- **詳細な分類**：同じクラス内でも目的が異なれば別の機能として分類
- **コンテキストの理解**：コードのコメントや周辺コードも読んで機能を正確に理解
- これは調査タスクなので、コード実装は不要
```

### Agent実行時の注意点

- **時間**: 数分から10分程度かかる可能性がある
- **並列実行不可**: 他のタスクは待つ
- **結果の信頼性**: Agent toolの結果は基本的に信頼できる

### 実績

GameTypeの調査では：
- **総使用ファイル数**: 57ファイル
- **機能分類数**: 33個
- **調査時間**: 約5分

---

## ステップ5: 機能別分類とドキュメント化

### 目的
- Agent toolの結果を整理してドキュメント化
- MOD実装の参考ポイントを追記
- プロジェクトドキュメントとして保存

### ドキュメント構成

```markdown
# [クラス名]使用箇所の機能別分類（Minecraft Forge 1.21.8）

## [クラス名] クラスの基本情報
[Agent toolの結果をそのまま記載]

## 機能分類
[Agent toolの結果をそのまま記載]

## 統計
[Agent toolの結果をそのまま記載]

## MOD開発への示唆

### [クラス名]と同様の機能を実装する場合の参考ポイント

#### 1. データ構造
[分析結果から学べるデータ構造の設計]

#### 2. 管理クラスの設計
[サーバー側/クライアント側の分離など]

#### 3. 永続化
[NBT保存の方法]

#### 4. ネットワーク同期
[パケット設計]

#### 5. 能力制御
[機能の適用方法]

#### 6. イベント統合
[Forgeイベントとの統合]

#### 7. UI統合
[ユーザーインターフェースへの統合]

#### 8. 実装が必要な最小限の箇所（必須）
[必須実装のリスト]

#### 9. 実装が推奨される箇所
[推奨実装のリスト]

#### 10. 実装不要な箇所
[スコープ外の機能リスト]
```

### ファイル保存場所

```
docs/[クラス名の小文字]-usage-analysis.md
```

例: `docs/gametype-usage-analysis.md`

### プロジェクトドキュメントへの統合

**1. タスクリストを更新**:
```markdown
1. 完了✅|[タスク名]
    * **調査完了**：[クラス名]の使用箇所を網羅的に調査し、XX の機能分類を特定（[詳細](/docs/[ファイル名].md)）
```

**2. CLAUDE.mdを更新**:
```markdown
# 進捗

直前の成果！: [クラス名]の使用箇所を網羅的に調査し、XXの機能分類を特定した。[目的]の参考資料として[ドキュメント名]を作成。

実行中: なし

すべての進捗: [タスクリスト](/docs/task_list.md)
```

---

## 調査のベストプラクティス

### DO（推奨）

✅ **Web検索から始める**
- 最新情報と旧バージョンのドキュメントを両方確認

✅ **Agent toolを積極的に使う**
- 網羅的な調査にはAgent toolが最適
- 人間の見落としを防ぐ

✅ **機能別分類は詳細に**
- 30以上の分類になっても問題ない
- 一つの機能=一つの分類

✅ **MOD開発への示唆を追記**
- 調査結果をどう活用するかを明記
- 必須/推奨/不要を明確に分類

✅ **ドキュメント化を徹底**
- 後で見返せるように詳細に記録
- プロジェクトドキュメント（CLAUDE.md、task_list.md）も更新

### DON'T（非推奨）

❌ **手動で全検索しない**
- 時間がかかり、見落としが発生する
- Agent toolの方が確実

❌ **分類を大雑把にしない**
- 「UI関連」のような大分類だけでは後で使いにくい
- 具体的な機能レベルで分類する

❌ **コード実装を始めない**
- 調査フェーズでは実装しない
- 理解が深まってから実装する

❌ **ドキュメント化を省略しない**
- 「後で書けばいい」は忘れる
- 調査直後にドキュメント化

---

## 実績例: GameType調査

### 調査時間
- **合計**: 約15分
  - Web検索: 3分
  - 旧バージョンJavadoc確認: 2分
  - ダミークラス作成・コンパイル: 2分
  - Agent tool実行: 5分
  - ドキュメント化: 3分

### 成果物
- **[GameType使用箇所分析](/docs/gametype-usage-analysis.md)**
  - 57ファイル、33機能分類
  - MOD実装のための10の参考ポイント

### 得られた知見
1. **サーバー・クライアント分離**: ServerPlayerGameModeとMultiPlayerGameModeで別々に管理
2. **前回の状態管理**: previousGameModeの存在とNullable型の使用
3. **能力制御の中央管理**: updatePlayerAbilities()メソッドの重要性
4. **ネットワーク同期**: CommonPlayerSpawnInfoでのID変換
5. **レガシーサポート**: LEGACY_ID_CODECによる互換性維持

---

## 他のクラスへの応用例

この手法は以下のようなクラス調査にも応用できます：

### Abilities（プレイヤー能力）
```
調査目的: MOD有効性による能力制限の実装
調査対象: net.minecraft.world.entity.player.Abilities
期待する発見: 飛行、無敵、即座に破壊などの能力フラグ
```

### Inventory（インベントリ）
```
調査目的: 1スロットインベントリの実装
調査対象: net.minecraft.world.entity.player.Inventory
期待する発見: スロット管理、アイテム追加、スロット制限
```

### Container（コンテナ）
```
調査目的: カスタムインベントリ画面の実装
調査対象: net.minecraft.world.inventory.AbstractContainerMenu
期待する発見: スロット登録、アイテム移動、同期処理
```

### HumanoidArm（腕）
```
調査目的: メインハンドの特定
調査対象: net.minecraft.world.entity.HumanoidArm
期待する発見: 左右の判定、メインハンド取得
```

---

## トラブルシューティング

### Agent toolが正しく動作しない

**症状**: Agent toolがエラーを返す、または結果が不完全

**対処法**:
1. デコンパイルソースのパスを再確認
2. 調査対象クラスの完全修飾名（パッケージ名含む）を指定
3. プロンプトを簡潔にして再試行

### 分類が多すぎて整理できない

**症状**: 50以上の分類になり、把握が困難

**対処法**:
1. 大分類（例: サーバー側、クライアント側）を導入
2. 関連する機能をグループ化
3. 優先度をつける（必須、推奨、オプション）

### 旧バージョンJavadocの情報が古い

**症状**: 1.16.5のJavadocと1.21.8で差異が大きい

**対処法**:
1. Agent toolの結果を優先
2. 差分を明記
3. Web検索で最新情報を補完

---

## 関連ドキュメント

- [バニラコード調査](/docs/vanilla-code-research.md) - ターゲットメソッドの特定方法
- [Mixin実装](/docs/mixin-implementation.md) - バニラコード改変の実装方法
- [Web検索・トラブルシューティング](/docs/research-troubleshooting.md) - 調査で詰まったときのガイド