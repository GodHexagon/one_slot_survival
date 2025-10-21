# 開発クイックスタートガイド

**いつ読むか**: 新しいタスクを開始するとき、または実装方法に迷ったとき

## タスク判断フローチャート

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
Forgeイベントでは対応できない？
    YES → [Mixin Implementation](#mixin-implementation)
    NO  ↓
エラーや問題が発生？
    YES → [Troubleshooting](#troubleshooting)
    NO  ↓
通常実装（既存パターン活用）
```

---

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

**重要な注意事項:**
- ⚠️ **Web検索の情報は古い可能性が高い** - Minecraft/Forgeは頻繁に変更される
- ✅ 戦略のヒント・アイデアとして活用する
- ✅ 得た情報は必ずJAR抽出やコンパイルで検証する
- ✅ 実装の詳細は検索せず、バニラコード/Forge APIで確認する

**Web検索が適している質問:**
- "Minecraft MOD インベントリ制限 実装方法"
- "プレイヤー 馬に乗ったときの移動速度を変更"
- "Forge カスタムスロット 他MOD 事例"
- "Mixin vs Forgeイベント 使い分け"

**Web検索が適していない質問:**
- "AbstractContainerScreen メソッド シグネチャ" → JAR抽出を使う
- "○○メソッドをMixinで変更" → JAR抽出を使う
- "NullPointerException 解決方法" → トラブルシューティングを使う
- "Forge 1.21.8 イベント一覧" → JAR抽出でForgeのソースを確認

---

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

**詳細ガイド:** [workflows/vanilla-research.md](./workflows/vanilla-research.md)

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

**詳細ガイド:** [workflows/forge-api-research.md](./workflows/forge-api-research.md)

---

## Mixin Implementation

**シチュエーション:**
- Forgeイベントが存在しない処理を変更したい
- GUI操作の根本的な制御が必要
- パフォーマンスが重要な箇所

**基本パターン:**
```java
@Mixin(TargetClass.class)
public class TargetClassMixin {
    @Inject(method = "targetMethod", at = @At("HEAD"), cancellable = true)
    private void onTargetMethod(CallbackInfo ci) {
        if (shouldIntercept()) {
            // カスタム処理
            ci.cancel();  // バニラ処理をキャンセル
        }
    }
}
```

**詳細ガイド:** [technical/mixin-guide.md](./technical/mixin-guide.md)

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

---

## クイックリファレンス

| やりたいこと | 最初に読むドキュメント |
|------------|---------------------|
| 実装方針を決めたい | [Strategy Research](#strategy-research) |
| バニラのメソッドを知りたい | [workflows/vanilla-research.md](./workflows/vanilla-research.md) |
| Forge APIの使い方を調査 | [workflows/forge-api-research.md](./workflows/forge-api-research.md) |
| Mixinで深い改変 | [technical/mixin-guide.md](./technical/mixin-guide.md) |
| JAR抽出の詳細手順 | [technical/source-extraction.md](./technical/source-extraction.md) |
| エラーが解決できない | [technical/troubleshooting.md](./technical/troubleshooting.md) |

---

## エージェント向けガイドライン

### タスク開始時のチェックリスト

- [ ] このドキュメント（quick-start.md）を読んだ
- [ ] タスクの性質を判断した（バニラ調査/Forge API/Mixin/トラブル解決）
- [ ] 該当する詳細ガイドを確認した
- [ ] 環境検出が必要な場合は `pwd` を実行した

### 困ったときの行動

1. **まずこのドキュメントに戻る** - フローチャートで再確認
2. **該当する詳細ガイドを読む** - 具体的な手順を確認
3. **ユーザーに質問する** - 情報が不足している場合

### やってはいけないこと

- ❌ エラー解決やAPI調査でWeb検索を優先する（JAR抽出が最優先）
- ❌ Task tool で general-purpose agent を起動する（ほとんどの場合不要）
- ❌ 環境検出をせずに決め打ちでパスを指定する
- ❌ 長時間試行錯誤する（適切なガイドを読めば解決する）
