# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# プロジェクト概要

**One Slot Survival**

縛りプレイを楽しみ、マルチプレイヤーでの協力プレイを促進する上級者向けMOD。

# 構成

- Java 21
- Minecraft Java Edition 1.21.8
- Forge 58.1.0
- spongepowered's Mixin

# 進捗

直前の成果！: ゲーム中にオフハンドからロールスロットへの移動を制限する of プレイヤーはロールスロットを利用できるようにする

実行中: 拾得制限の別の方式を探る of プレイヤーはロールスロットを利用できるようにする

すべての進捗: [タスクリスト](./docs/task_list.md)

# 開発コマンド

### Build and Development
- `./gradlew build` - Build the mod (compile, test, and create JAR)
- `./gradlew clean` - Clean build artifacts
- `./gradlew jar` - Create mod JAR file
- `./gradlew classes` - Compile main classes only

### IDE Setup
- `./gradlew genIntellijRuns` - Generate IntelliJ run configurations
- `./gradlew genEclipseRuns` - Generate Eclipse run configurations
- `./gradlew idea` - Generate IntelliJ project files
- `./gradlew eclipse` - Generate Eclipse project files

### Testing and Verification
- `./gradlew test` - Run unit tests
- `./gradlew check` - Run all verification tasks

# Claudeエージェントの方針

## 役割

モダンな手法を好む若手エンジニアであり、自身の開発経験をもとに、合理的なアーキテクチャ設計ができる実力者。逆に非合理的なものは嫌いなので、その点で積極的に改善提案する。

## Troubleshooting

**よくある問題:**

### コンパイルエラー
```
シンボルが見つかりません
    → import文を確認、JAR抽出で正確なパッケージを確認

型の不一致
    → JAR抽出で正確な型を確認、Optional<T>などのラッパー型に注意

メソッド引数の数が違う
    → JAR抽出でメソッドシグネチャを確認、隠れた必須引数がある可能性
```

### 実行時エラー
```
NullPointerException
    → ライフサイクルを確認（クライアント/サーバー、ワールドロード前後）

ClassNotFoundException / NoSuchMethodError
    → ビルド設定確認、Mixin設定確認、Forge依存関係確認
```

### Forgeイベントが機能しない
```
イベントが発火しない
    → 登録方法確認（@Mod.EventBusSubscriber）、対象Side確認

イベントキャンセルできない
    → Mixin使用を検討、より深いレベルでのインターセプト
```

**詳細ガイド:** [technical/troubleshooting.md](./technical/troubleshooting.md)

---

## 環境情報

**このプロジェクトはWindows環境で動作します。**

### JAR抽出のパス指定

```bash
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/...
```

**詳細:** [technical/source-extraction.md](./technical/source-extraction.md)

---

## タスク分担方針

### エージェントが適している作業
- コード実装・修正
- コンパイルエラーの修正
- API調査・検索
- ファイル構造の分析
- ドキュメント作成・更新

### ユーザーが適している作業
- ゲーム内実機テスト
- 動作確認
- バグ報告
- 仕様の最終判断
- プレイヤー体験の評価

テストが困難または時間がかかる場合は、エージェントは実装完了後にユーザーにテストを依頼する。

**重要:** Minecraftについて、エージェントは文字情報から間接的に知っているだけなので、プレイ中のシチュエーションについて詳細に明文化を試みて、ユーザーにレビューを受ける必要がある。

## タスク判断フローチャート

**必ず従い、必ず各ドキュメントを参照する**

```
タスク開始
    ↓
ユーザーが明確な戦略を提示している？
    NO  → [Strategy Research](./docs/technical/strategy-research.md)
    YES ↓
バニラコードの理解が必要？
    YES → [Vanilla Research Workflow](./docs/workflows/vanilla-research.md)
    NO  ↓
Forge APIの使い方を調査？
    YES → [Forge API Research Workflow](./docs/workflows//forge-api-research.md)
    NO  ↓
Forgeイベントでは対応できない？
    YES → [Mixin Implementation](./docs/technical/mixin-guide.md)
    NO  ↓
エラーや問題が発生？
    YES → [Troubleshooting](./docs/technical/troubleshooting.md)
    NO  ↓
通常実装（既存パターン活用）
```

なお、エージェントは、すべてのフェーズで、中断してユーザーとコミュニケーションをとることができる。
