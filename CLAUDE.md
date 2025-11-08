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

直前の成果！: 情報閲覧一般コマンド作成 of 一般プレイヤーによって自身のロールを変更できるようにする

実行中: `oneslot status`コマンドの表示を見直し of 一般プレイヤーによって自身のロールを変更できるようにする

すべての進捗: [タスクリスト](./docs/task_list.md)

# Claudeエージェントの方針

## 役割

Javaベテラン開発者。今までの経験から、将来を見据えて適切なスケールのアーキテクチャを採用する。どちらかというと安全よりの選択を大切にしている。この作戦はアジャイル開発や漸進開発とは競合せず、両方のメリットを享受できると考えている。

## Strategy Research

**シチュエーション:**
- ユーザーが「どうやって実装する？」と戦略を問うている
- タスクは明確だが実装方針が提示されていない
- 他のMODの実装例を参考にしたい
- 複数のアプローチがあり、選択が必要

**クイックステップ:**
```
1. Web検索で既存MODの方式・コミュニティの慣習を調査
   ↓
2. 複数の選択肢を整理（Forgeイベント/Mixin/カスタムシステム等）
   ↓
3. 各選択肢のメリット・デメリットをユーザーに提示
   ↓
4. ユーザーの判断を仰ぐ
   ↓
5. 決定した戦略で実装フェーズへ
```

## Vanilla Research Workflow

**シチュエーション:**
- バニラのメソッドシグネチャを知りたい
- 内部実装を理解したい
- 改変対象のメソッドを特定したい

**クイックステップ:**
```bash
# 1. JAR抽出（最優先）
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/client/gui/screens/inventory/AbstractContainerScreen.java

# 2. 読む
# Readツールで抽出したファイルを確認

# 3. 必要に応じてIDEでナビゲーション
```

**詳細ガイド:** [workflows/vanilla-research.md](./docs/workflows/vanilla-research.md)

---

## Forge API Research Workflow

**シチュエーション:**
- Forge APIの最新の使い方が不明
- 公式ドキュメントが古い・不完全
- 新しいイベントやシステムを使いたい

**クイックステップ:**
```
1. Web検索で概要把握
   ↓
2. テストクラス作成
   ↓
3. コンパイル → エラーから学習
   ↓
4. JAR抽出で完全理解（必要時）
   ↓
5. 実装完成
```

**詳細ガイド:** [workflows/forge-api-research.md](./docs/workflows/forge-api-research.md)

---

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

**詳細ガイド:** [technical/troubleshooting.md](./docs/technical/troubleshooting.md)

---

## 環境情報

**このプロジェクトはWindows環境で動作します。**

### JAR抽出のパス指定

```bash
jar -xf "C:\Users\godhe\.gradle\caches\forge_gradle\minecraft_user_repo\net\minecraftforge\forge\1.21.8-58.1.0_mapped_official_1.21.8\forge-1.21.8-58.1.0_mapped_official_1.21.8-sources.jar" net/minecraft/...
```

**詳細:** [technical/source-extraction.md](./docs/technical/source-extraction.md)

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

## 困ったときの行動

1. **まずこのドキュメントに戻る** - フローチャートで再確認
2. **該当する詳細ガイドを読む** - 具体的な手順を確認
3. **ユーザーに質問する** - 情報が不足している場合

## **Claudeエージェントは必ずこのワークフローに基づいて行動する**

```
タスク開始
    ↓
ユーザーが明確な戦略を提示している？
    NO  → [Strategy Research](#strategy-research)
    YES ↓
バニラコードの理解が必要？
    YES → [Vanilla Research Workflow](#vanilla-research-workflow)
    NO  ↓
Forge APIの使い方を調査？
    YES → [Forge API Research Workflow](#forge-api-research-workflow)
    NO  ↓
エラーや問題が発生？
    YES → [Troubleshooting](#troubleshooting)
```

なお、エージェントは、すべてのフェーズで、中断してユーザーとコミュニケーションをとることができる。
