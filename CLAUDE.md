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

- ワールドを再開するとプレイヤー有効性が無効に変更される。

## Newest Updates

「1. プレイヤーのホットバーは１スロットだけ（これをメインハンドと定義する）になり、それ以外のインベントリとホットバー計３５スロットを使用禁止する。」の部分的実装

✅ 実装済み機能

1. プレイヤー状態管理 (OneSlotManager.java)
   - シンプルなHashSet-basedでプレイヤーのUUIDを管理
   - Capabilityシステムを避けて最もシンプルな実装
2. 管理者コマンド (OneSlotCommand.java)
   - /oneslot <players> enable/disable/toggle/status
   - /oneslot status (自分の状態確認)
   - OP権限(レベル2)が必要
3. ホットバー強制 (OneSlotEvents.java)
   - プレイヤーTickイベントでスロット0に強制
   - inventory.pickSlot(0) を使用
4. 適切なパッケージ構造
   - com.github.godhexagon.oneslotsurvival

🧪 次のテスト

実際にMinecraftで動作をテストして、以下を確認できます：

1. コマンドが正常に動作するか
2. ホットバー強制が機能するか
3. プレイヤー状態が正しく管理されるか

📝 今後の拡張予定

- アイテム拾得制限
- インベントリアクセス制限
- ロールスロット機能
- データ永続化

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
    // 1. ホットバー強制（既存）
    // 2. 禁止スロットクリア（新規）
    // 3. アイテム拾得制限（新規）
}
```

**フェーズ2: 透明バリアアイテム追加**
- InvisibleBarrierItem.java - 完全透明のロック用アイテム
- InventoryLockSystem.java - バリア配置・除去システム
- SlotClickEventHandler.java - スロット操作制御

**フェーズ3: ユーザーエクスペリエンス改善**
- VisualFeedback.java - スロット状態の視覚的フィードバック
- SoundEffects.java - 禁止操作時のサウンド
- HelpSystem.java - 制限説明のヘルプ表示

### 技術的な利点
- **段階的実装**：フェーズ1だけでも基本機能は動作
- **後方互換性**：既存のOneSlotManagerシステムを活用
- **拡張性**：将来のロールスロット機能への発展が容易
- **パフォーマンス**：必要最小限のイベント処理

### 重要な実装ポイント
1. **既存アイテム処理**：スロット制限有効化時、既存アイテムをメインハンドに統合 or 地面ドロップ
2. **アイテム拾得制限**：メインハンド容量チェック後に拾得可否決定
3. **クリエイティブモード対応**：クリエイティブモードでは制限を緩和
4. **データ永続化**：プレイヤーログアウト時の状態保持
