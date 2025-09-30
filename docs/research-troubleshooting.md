## Web検索・トラブルシューティング

**いつ読むか**: API調査で詰まったとき、エラーが解決できないとき、または効率的な情報収集方法を知りたいとき

### Web検索の戦略的アプローチ

**検索キーワードの段階的絞り込み**
```
段階1: "Minecraft Forge 1.21 inventory slot click cancel"
段階2: "ScreenEvent.MouseButtonPressed.Pre cancel method"
段階3: "AbstractContainerScreen slotClicked method signature"
```

**複数情報源の活用パターン**
- 公式ドキュメント → フォーラム → GitHub Issues の順
- 古いバージョン情報から最新版への類推
- 成功事例（他MOD）の参考

### Forgeソースコード解析テクニック

**Forge GitHub解析アプローチ**
- **MinecraftForge/MinecraftForge** リポジトリの活用
- **Issues検索**: 同様の問題を抱えた開発者の解決策
- **Commit履歴**: APIの変更点や推奨実装方法
- **PR（Pull Request）**: 新機能や修正の実装例

**Forgeフォーラム活用法**
```
検索パターン例:
- "[SOLVED][1.21.8] How to cancel InputEvent.MouseScrollingEvent?"
- "AbstractContainerScreen mouseClicked override"
- "Mixin vs Forge Event performance comparison"
```

**Forge API階層の理解**
```
バニラMinecraft → Forge拡張 → ModAPI の層構造
├─ Vanilla: AbstractContainerScreen#slotClicked
├─ Forge: ScreenEvent.MouseButtonPressed.Pre
└─ Mod: カスタムMixin実装
```

### 問題解決時の工夫

**「イベントキャンセル失敗」→「代替アプローチ」**
```java
// 最初の試み：ScreenEvent.MouseButtonPressed.Preをキャンセル
event.setCanceled(true); // ❌ メソッドが見つからない

// 解決策：Mixinで根本的処理をインターセプト
@Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
private void onSlotClicked(..., CallbackInfo ci) {
    ci.cancel(); // ✅ 完璧に動作
}
```

**「段階的実装」戦略**
- Phase 3.2: スクロール無効化（リフレクション使用）
- Phase 3.4: クリック無効化（Mixin使用）
- 各段階で確実な動作確認

### トラブルシューティング実例

**MouseScrollingEventの挫折 → 学習機会**
- `setCanceled`メソッドが利用できない
- → リフレクション使用でホットバー制御に切り替え
- → 結果的により安定した実装

**Forgeイベント制限の発見 → Mixin採用**
- ScreenEvent系のキャンセレーション問題
- → Mixinによる根本的解決
- → より強力で確実な制御を実現

### Forge開発の進化理解

**イベントシステムの変遷**
```java
// 古い方式 (Forge 1.12.x)
@SubscribeEvent
public void onGuiOpen(GuiOpenEvent event) { ... }

// 現代的方式 (Forge 1.21.x)
@SubscribeEvent
public void onScreenOpen(ScreenEvent.Opening event) { ... }
```

**Forgeバージョン間差異の調査**
- **1.19.x → 1.21.x**: APIの破壊的変更
- **NeoForge分岐**: 2024年以降の選択肢
- **マッピング変更**: MCP → 公式Mojang Mappings

**Mixin統合の背景**
- Forgeイベントでカバーできない領域の存在
- パフォーマンス重視の実装需要
- Fabric MODとの互換性向上

**公式サポート状況の把握**
- **推奨手法**: 公式Forgeイベント優先
- **許容手法**: Mixin（最小限使用）
- **非推奨**: ASM直接操作、リフレクション乱用

### 知識蓄積の工夫

**「失敗も含めた記録」**
- 動作しなかった方法も記録（将来の参考）
- エラーメッセージと解決策をセット保存

**「再利用可能なテンプレート化」**
- build.gradle設定をテンプレート化
- Mixin基本構造をパターン化
- 成功したワークフローを体系化