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

直前の成果！: 毎ティックスロットバリア配布処理がされる問題を修正する of プレイヤーはロールスロットを利用できるようにする

実行中: クイック移動のときに普通にロールスロットに入る問題を修正する of プレイヤーはロールスロットを利用できるようにする

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

## 必ず従うワークフロー

**新しいタスクを開始するとき:**
- **必ず [Quick Start Guide](./docs/quick-start.md) を読む**
- タスクの性質を判断（バニラ調査/Forge API/Mixin/トラブル解決）
- 該当する詳細ガイドを参照

**困ったとき【冷静になるの大事】:**
- **まず [Quick Start Guide](./docs/quick-start.md) に戻る**
- フローチャートで再確認
- 該当する詳細ガイドを読む
- 解決できない場合はユーザーに質問

なお、エージェントは、すべてのフェーズで、中断してユーザーとコミュニケーションをとることができる。

## ドキュメント一覧

**クイックスタート:**
- [Quick Start Guide](./docs/quick-start.md) - タスク開始時・困ったときに読む判断フローチャート

**ワークフロー:**
- [バニラコード調査](./docs/workflows/vanilla-research.md) - バニラMinecraftの内部実装を理解したいとき
- [Forge API調査](./docs/workflows/forge-api-research.md) - Forge APIの使い方を調査したいとき

**技術詳細:**
- [JAR抽出ガイド](./docs/technical/source-extraction.md) - ソースコード抽出の完全ガイド
- [Mixin実装ガイド](./docs/technical/mixin-guide.md) - Forgeイベントでは対応できない深いレベルでの動作変更
- [トラブルシューティング](./docs/technical/troubleshooting.md) - エラー解決・問題解決戦略
