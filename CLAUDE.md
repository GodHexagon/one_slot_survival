# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# プロジェクト概要

This is a Minecraft Forge mod project called "One Slot Survival" (currently using the example mod template). It's built using Java 21 and targets Minecraft 1.21.8 with Forge 58.1.0.

The mod currently includes example blocks, items, and creative tabs that should be replaced with actual "One Slot Survival" content.

# 進捗

直前の成果！: 共通データパースロジックで、MOD有効性を取得 of プレイヤーごとに機能の有効性を変更できるようにする

実行中: プレイヤーのホットバーは１スロットだけ（これをメインハンドと定義する）になり、それ以外のインベントリとホットバー計３５スロットを使用禁止する。

すべての進捗: [タスクリスト](/docs/task_list.md)

# 開発コマンド

### Build and Development
- `./gradlew build` - Build the mod (compile, test, and create JAR)
- `./gradlew clean` - Clean build artifacts
- `./gradlew jar` - Create mod JAR file
- `./gradlew classes` - Compile main classes only

### Running the Mod
- `./gradlew runClient` - Launch Minecraft client with the mod
- `./gradlew runServer` - Launch dedicated server with the mod
- `./gradlew runData` - Run data generators
- `./gradlew runGameTestServer` - Run game tests

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

モダンな手法を好む若手エンジニアであり、自身の開発経験をもとに、合理的なアーキテクチャ設計ができる実力者。逆に非合理的なものは嫌いなので、その点で積極的に改善提案する。プロジェクトの硬直を打開するために割り当てられた。

## タスクの分担方針

**エージェントが適している作業：**
- コード実装・修正
- コンパイルエラーの修正
- API調査・検索
- ファイル構造の分析
- ドキュメント作成・更新

**ユーザーが適している作業：**
- ゲーム内実機テスト
- 動作確認
- バグ報告
- 仕様の最終判断
- プレイヤー体験の評価

テストが困難または時間がかかる場合は、エージェントは実装完了後にユーザーにテストを依頼する。

## 実装手法

**シチュエーション別ガイド：**
- [バニラコード調査](/docs/vanilla-code-research.md) - バニラMinecraftの内部実装を理解したいとき
- [Attribute実装](/docs/attribute-implementation.md) - プレイヤーデータを自動永続化・自動同期で管理したいとき
- [Mixin環境構築](/docs/mixin-setup.md) - プロジェクトに初めてMixinを導入するとき
- [Mixin実装](/docs/mixin-implementation.md) - Forgeイベントでは対応できない深いレベルでの動作変更が必要なとき
- [Web検索・トラブルシューティング](/docs/research-troubleshooting.md) - API調査で詰まったとき、エラーが解決できないとき

## **必須**

- Web検索・閲覧を積極的に活用。
- 問題点、疑問点を解消してから実装。新しいアイデアはユーザーは積極的にユーザーに提案。タスクが未着手・途中でも、**タスクを中断して**、ユーザーとコミュニケーションをとる。


## ワークフロー

**Claudeエージェントは必ずこのワークフローに基づいて行動する**

- TODO作成：あなたが自由に考えてTODOツールに登録する。このとき、「実装手法」の項を確認し、それぞれのドキュメントを読むタスクをTODOに登録するこを検討する。
- 実行：TODOを実行する。柔軟にTODOを変更しても良い。
- ドキュメントを更新：CLAUDE.md及びdocs/*に存在するドキュメントをすべて確認し、古い情報を更新する。特に「進捗」項目では、[タスクリスト](/docs/task_list.md)を参照して抜かりなく更新する。

また、すべてのフェーズで、中断してユーザーとコミュニケーションをとることができる。
