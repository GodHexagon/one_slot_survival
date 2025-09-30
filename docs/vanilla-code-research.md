## バニラコード調査・解析

**いつ読むか**: バニラMinecraftの内部実装を理解したい、または改変対象のメソッドを特定したいとき

### ターゲットメソッドの特定

**「ダミー継承クラス」作戦**
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

**段階的メソッド確認**
- 基本メソッド（mouseClicked）→ 専門メソッド（slotClicked）の順で調査
- 各段階でコンパイルして、利用可能なメソッドを確認
- 必要なimportを段階的に追加

### 利用可能ツール

- **MinecraftDecompiler**: MC 1.21 JARファイルの自動デコンパイル
- **McDeob**: 公式Mojang Mappingsを使用した高速リマッピング（約6秒）
- **公式Mojang Mappings**: MC 1.21で利用可能、NeoForge 1.20.2+では標準

### 開発環境での確認

- 既存の開発環境にForge 1.21が設定済みの場合、IDEで直接クラスを確認可能
- テストクラス作成→コンパイル→署名確認のサイクル

### 「コンパイル駆動開発」

- テストクラス作成 → コンパイルエラー → 署名判明
- エラーメッセージから正しいAPIを推測
- IDEの自動補完を最大限活用