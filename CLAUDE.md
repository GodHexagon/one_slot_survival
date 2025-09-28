# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Minecraft Forge mod project called "One Slot Survival" (currently using the example mod template). It's built using Java 21 and targets Minecraft 1.21.8 with Forge 58.1.0.

The mod currently includes example blocks, items, and creative tabs that should be replaced with actual "One Slot Survival" content.

## Development Commands

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

## Claudeエージェントの方針

適切なバージョンの正確な情報を得るため、エージェントは困ったらすぐにWeb検索・閲覧を行います。

躓いたり、迷ったりしたことがあれば、タスクを中断して、ユーザーに確認します。

### タスクの分担方針

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

## タスクリスト

### 機能

1. 完了✅|プレイヤーごとに機能の有効性を変更できるようにする。
    * 管理者権限を持つ者だけが実行できる、管理者アクションの一つとする。
1. プレイヤーのホットバーは１スロットだけ（これをメインハンドと定義する）になり、それ以外のインベントリとホットバー計３５スロットを使用禁止する。
   * メインハンドがいっぱいになった場合、バニラでインベントリがいっぱいになっときみたいに、新たにアイテムを拾うことができなくなる。
   * メインハンド以外のスロットを保持（ホットバー選択）できない。
1. プレイヤーはロールスロットを利用できるようにする。
   * プレイヤーはメインハンドのほかに、ロールスロットがホットバーとしてあり、ホットバー選択できる。
     * メインスロットの右側に３つある。
     * つるはししか入れられない。
     * ゲーム中は通常のホットバーと同じ挙動をする。
     * インベントリ中では、防具スロットのように専用スロットとしての挙動をする。
     * ドロップしたつるはしを拾う際は普通に入る。
     * オフハンドからつるはし以外をFキーで直接入れようしても、なにも起こらない。
1. 右から１つ目と２つ目のロールスロットはシャベルしか入らないようにする。
1. プレイヤーに割り当てられたメインロールによって、ロールスロットに入れることができるアイテムが変わるようにする。
    * 今までのロールスロットはMinerのものとし、新たにWarriorを追加する。
1. 各プレイヤーのロールを変更する管理者アクションを追加。
1. 管理者アクションをGUI操作できるようにする。
1. カスタムインベントリ画面と互換性インベントリ画面を追加する。
    * どちらの画面かのワールドオプションを変更する管理者アクションをを追加する。
1. メインロールに紐づくEXPとレベルアップがあり、これによりロールスロットをカスタムできるようにする。
1. 一般プレイヤーによって自身のロールを変更できるようにする。
    * インベントリに、ロール変更メニューが追加される。
    * ワールドのロール変更可能性オプションを変更する管理者アクションを追加。
1. 全プレイヤーロール一斉割り当ての管理者アクションを追加する。
1. ワールドの初期ロール割り当て方式オプションを変更する管理者アクションを追加。
1. それ以外の管理者アクションを追加。
1. メインロールの種類を増やす。
1. サブロールを追加する。

### 問題

## 計画２「35スロット制限」設計

### 要件詳細
- ホットバーはメインハンド（スロット0）のみ使用可能
- 他の35スロット（ホットバー1-8 + インベントリ9-35）を使用禁止
- メインハンドがフルの場合、アイテム拾得不可
- メインハンド以外のスロット保持（選択）不可

### 実装方式：ハイブリッド方式（推奨）

**フェーズ1: 基本制限（即座に実装可能）**
```java
// OneSlotEvents.javaを拡張
@SubscribeEvent
public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
    // 1. ホットバー強制（既存） -> フェーズ３で確認
    // 2. 禁止スロットクリア（新規）
    // 3. アイテム拾得制限（新規） -> フェーズ２で確認
}
```

**フェーズ１完了・テスト項目**

- 禁止スロットにスタック可能アイテム配置
- 禁止スロットにスタック可能アイテム配置（スタックオーバーフロー）
- 剣をメインスロット及び他のスロットに複数配置

**フェーズ2: 透明バリアアイテム追加**
- アイテム拾得制限になるか確認
- アイテムが減ったり増えたりバリアアイテムがチェストに入ったりしないか確認 -> フェーズ３の時に確認

**フェーズ2・テスト項目**
- メインスロットがいっぱいの時にドロップアイテムの近くに行き、拾得しないことを確認

**フェーズ3: クライアントサイドの変更**
- インベントリ・ホットバーの見た目を変更、そもそもスロットがないように見せかける。
- ゲーム中ではスクロール操作しても何も起きないように変更
- インベントリ画面で不要な部分をクリックして何も起こらないか確認
- `barrier-item-prevention-plan.md`を参考に、問題が起こらないか検証

### 重要な実装ポイント
1. **既存アイテム処理**：スロット制限有効化時、既存アイテムをメインハンドに統合 or 地面ドロップ
2. **アイテム拾得制限**：メインハンド容量チェック後に拾得可否決定
3. **クリエイティブモード対応**：クリエイティブモードでは制限を緩和
4. **データ永続化**：プレイヤーログアウト時の状態保持
