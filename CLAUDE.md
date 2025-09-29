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

## Minecraft Forge Mixinワークフロー

### バニラソースコード調査 → Mixin改変プラクティス

Forgeイベントでは対応できない深いレベルでの動作変更が必要な場合、Mixinを使用してバニラコードを直接改変する。

#### フェーズ1: ソースコード調査・解析

**1.1 ターゲットメソッドの特定**
```java
// テストクラスを作成してメソッド署名を確認
public class TestTargetClass extends TargetClass {
    @Override
    public ReturnType targetMethod(Parameters...) {
        // IDEでオーバーライドすることで正確な署名を取得
        return super.targetMethod(parameters);
    }
}
```

**1.2 利用可能ツール**
- **MinecraftDecompiler**: MC 1.21 JARファイルの自動デコンパイル
- **McDeob**: 公式Mojang Mappingsを使用した高速リマッピング（約6秒）
- **公式Mojang Mappings**: MC 1.21で利用可能、NeoForge 1.20.2+では標準

**1.3 開発環境での確認**
- 既存の開発環境にForge 1.21が設定済みの場合、IDEで直接クラスを確認可能
- テストクラス作成→コンパイル→署名確認のサイクル

#### フェーズ2: Mixin環境構築

**2.1 build.gradle設定**
```gradle
plugins {
    // 既存のプラグイン...
    id 'org.spongepowered.mixin' version '0.7.+'
}

dependencies {
    // Mixinアノテーションプロセッサ（必須）
    annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'
}

// Mixin設定ブロック
mixin {
    add sourceSets.main, "modid.refmap.json"
    config "modid.mixins.json"
}

minecraft {
    runs {
        configureEach {
            arg "-mixin.config=${mod_id}.mixins.json"
        }
    }
}

tasks.named('jar', Jar) {
    manifest {
        attributes['MixinConfigs'] = "${mod_id}.mixins.json"
    }
}
```

**2.2 Mixin設定ファイル作成**
```json
// src/main/resources/modid.mixins.json
{
  "required": true,
  "package": "com.yourmod.mixin",
  "compatibilityLevel": "JAVA_21",
  "refmap": "modid.refmap.json",
  "minVersion": "0.8",
  "client": [
    "YourMixinClass"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

#### フェーズ3: Mixin実装

**3.1 基本的なMixin構造**
```java
@Mixin(TargetClass.class)
public class TargetClassMixin {

    @Inject(method = "targetMethod", at = @At("HEAD"), cancellable = true)
    private void onTargetMethod(Parameters parameters, CallbackInfo ci) {
        // 条件判定
        if (shouldIntercept()) {
            // カスタム処理
            handleCustomLogic();

            // バニラ処理をキャンセル
            ci.cancel();
        }
    }
}
```

**3.2 成功例: AbstractContainerScreenMixin**
```java
@Mixin(AbstractContainerScreen.class)
public class AbstractContainerScreenMixin {

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void onSlotClicked(Slot slot, int slotId, int mouseButton, ClickType type, CallbackInfo ci) {
        if (OneSlotClientManager.isLocalPlayerRestricted() &&
            slot != null &&
            BarrierItem.shouldHaveBarrier(slot.getSlotIndex())) {

            // フィードバック
            Minecraft.getInstance().player.displayClientMessage(
                Component.literal("§c[One Slot] This slot is disabled"), true
            );

            // 完全キャンセル
            ci.cancel();
        }
    }
}
```

#### フェーズ4: 検証・テスト

**4.1 コンパイル確認**
- SpongePowered MIXIN Annotation Processor動作確認
- refmap.json生成成功
- searge mappings適用成功

**4.2 実機テスト項目**
- 条件判定の正確性
- バニラ処理の完全キャンセル
- ユーザーフィードバックの表示
- 他MODとの互換性

#### ベストプラクティス

**🔧 技術的要点**
- `@At("HEAD")`: メソッド開始時点での処理
- `cancellable = true`: CallbackInfoによるキャンセル可能
- `ci.cancel()`: バニラ処理の完全バイパス
- 条件判定: 既存システムとの連携

**⚠️ 注意点**
- Mixinは強力だが互換性リスクあり
- 最小限の改変に留める
- 適切なテストが必須
- ドキュメント化を怠らない

**🚀 適用場面**
- Forgeイベントが存在しない処理
- 既存イベントでは不十分な制御が必要
- パフォーマンスが重要な箇所
- GUI操作の根本的変更

#### 🔍 調査テクニック・実践的コツ

**💡 メソッド署名の効率的調査法**

1. **「ダミー継承クラス」作戦**
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

2. **段階的メソッド確認**
   - 基本メソッド（mouseClicked）→ 専門メソッド（slotClicked）の順で調査
   - 各段階でコンパイルして、利用可能なメソッドを確認
   - 必要なimportを段階的に追加

**🎯 Web検索の戦略的アプローチ**

1. **検索キーワードの段階的絞り込み**
```
段階1: "Minecraft Forge 1.21 inventory slot click cancel"
段階2: "ScreenEvent.MouseButtonPressed.Pre cancel method"
段階3: "AbstractContainerScreen slotClicked method signature"
```

2. **複数情報源の活用パターン**
   - 公式ドキュメント → フォーラム → GitHub Issues の順
   - 古いバージョン情報から最新版への類推
   - 成功事例（他MOD）の参考

**⚡ 問題解決時の工夫**

1. **「イベントキャンセル失敗」→「代替アプローチ」**
```java
// 最初の試み：ScreenEvent.MouseButtonPressed.Preをキャンセル
event.setCanceled(true); // ❌ メソッドが見つからない

// 解決策：Mixinで根本的処理をインターセプト
@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
private void onSlotClicked(..., CallbackInfo ci) {
    ci.cancel(); // ✅ 完璧に動作
}
```

2. **「段階的実装」戦略**
   - Phase 3.2: スクロール無効化（リフレクション使用）
   - Phase 3.4: クリック無効化（Mixin使用）
   - 各段階で確実な動作確認

**🛠️ デバッグ・検証のベストプラクティス**

1. **「コンパイル駆動開発」**
   - テストクラス作成 → コンパイルエラー → 署名判明
   - エラーメッセージから正しいAPIを推測
   - IDEの自動補完を最大限活用

2. **「実装前の動作確認」**
```java
// Mixinアノテーションプロセッサの動作ログ
// ノート: SpongePowered MIXIN Annotation Processor Version=0.8.5
// ノート: Writing refmap to [...]/compileJava-refmap.json
// → 正常に動作している証拠
```

**🎪 トラブルシューティング実例**

1. **MouseScrollingEventの挫折 → 学習機会**
   - `setCanceled`メソッドが利用できない
   - → リフレクション使用でホットバー制御に切り替え
   - → 結果的により安定した実装

2. **Forgeイベント制限の発見 → Mixin採用**
   - ScreenEvent系のキャンセレーション問題
   - → Mixinによる根本的解決
   - → より強力で確実な制御を実現

**📚 知識蓄積の工夫**

1. **「失敗も含めた記録」**
   - 動作しなかった方法も記録（将来の参考）
   - エラーメッセージと解決策をセット保存

2. **「再利用可能なテンプレート化」**
   - build.gradle設定をテンプレート化
   - Mixin基本構造をパターン化
   - 成功したワークフローを体系化

これらのテクニックにより、**未知のAPI調査→実装→動作確認**のサイクルを効率化できた。

**🔬 Forgeソースコード解析テクニック**

1. **Forgeイベント体系の理解**
```java
// 今回の発見：ScreenEvent系の制約
ScreenEvent.MouseButtonPressed.Pre event;
// event.setCanceled(true); // ❌ 利用できない場合がある
```

2. **Forge GitHub解析アプローチ**
   - **MinecraftForge/MinecraftForge** リポジトリの活用
   - **Issues検索**: 同様の問題を抱えた開発者の解決策
   - **Commit履歴**: APIの変更点や推奨実装方法
   - **PR（Pull Request）**: 新機能や修正の実装例

3. **Forgeフォーラム活用法**
```
検索パターン例:
- "[SOLVED][1.21.8] How to cancel InputEvent.MouseScrollingEvent?"
- "AbstractContainerScreen mouseClicked override"
- "Mixin vs Forge Event performance comparison"
```

4. **Forge API階層の理解**
```
バニラMinecraft → Forge拡張 → ModAPI の層構造
├─ Vanilla: AbstractContainerScreen#slotClicked
├─ Forge: ScreenEvent.MouseButtonPressed.Pre
└─ Mod: カスタムMixin実装
```

5. **Forgeバージョン間差異の調査**
   - **1.19.x → 1.21.x**: APIの破壊的変更
   - **NeoForge分岐**: 2024年以降の選択肢
   - **マッピング変更**: MCP → 公式Mojang Mappings

**📈 Forge開発の進化理解**

1. **イベントシステムの変遷**
```java
// 古い方式 (Forge 1.12.x)
@SubscribeEvent
public void onGuiOpen(GuiOpenEvent event) { ... }

// 現代的方式 (Forge 1.21.x)
@SubscribeEvent
public void onScreenOpen(ScreenEvent.Opening event) { ... }
```

2. **Mixin統合の背景**
   - Forgeイベントでカバーできない領域の存在
   - パフォーマンス重視の実装需要
   - Fabric MODとの互換性向上

3. **公式サポート状況の把握**
   - **推奨手法**: 公式Forgeイベント優先
   - **許容手法**: Mixin（最小限使用）
   - **非推奨**: ASM直接操作、リフレクション乱用

**🎯 適切な実装判断基準**

```
判断フロー（実践的優先順位）:
1. 【最優先】既存MODの実装を参照
   - CurseForge/Modrinthで類似機能のMOD検索
   - GitHubでオープンソースMODのコード確認
   - 成功事例の手法をベースに採用

2. 公式Forgeイベントで実現可能？ → YES: Forgeイベント使用
3. 既存イベントで十分な制御？ → NO: カスタムイベント検討
4. パフォーマンス要求厳しい？ → YES: Mixin検討
5. 他MOD互換性重要？ → YES: 標準的手法選択
6. 最終手段 → Mixin + 十分なテスト + ドキュメント化
```

**🔍 既存MOD実装参照の具体的手法**

1. **類似機能MOD検索**
```
検索例：
- "inventory management" "hotbar disable" - インベントリ制御MOD
- "Mouse Tweaks" "Inventory Tweaks" - 既存の有名MOD
- "NoWheel" "Disable Hotbar Scrolling" - 今回参考にしたMOD
```

2. **GitHub実装調査**
```java
// 実際の参考例: NoWheelMod的な実装
@SubscribeEvent
public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
    if (shouldDisableScroll()) {
        // 実装者がどの方法を選んだかを確認
    }
}
```

3. **実装パターンの分類**
   - **Forge標準**: 公式イベント使用（安定・互換性◎）
   - **Mixin重用**: 高度な制御（強力・リスク中）
   - **ハイブリッド**: ForgeEvent + Mixin（柔軟・複雑）

この体系的アプローチにより、**Forge生態系全体を理解した上での最適な実装選択**が可能になる。
