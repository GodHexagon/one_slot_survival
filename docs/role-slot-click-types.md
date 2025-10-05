# ロールスロットに関連する ClickType 一覧

## 調査日
2025年10月5日

## 概要
ロールスロット（ホットバーのインデックス1-3）に対して実行される可能性のある ClickType を特定しました。

## 関連する ClickType

### 1. **ClickType.SWAP** ← **最重要**
- **操作**: 数字キー（1-9）またはFキー（オフハンド）
- **パラメータ**: `button` = 0-8（ホットバー）または 40（オフハンド）
- **動作**: ホットバーのアイテムとクリックされたスロットのアイテムを入れ替える
- **問題**: RoleSlotBarrierアイテムがホットバーに移動してしまう
- **対応**: 必須

### 2. **ClickType.PICKUP**
- **操作**: 左クリック（button=0）または右クリック（button=1）
- **動作**: アイテムをピックアップまたは配置
- **対応**: 既存の AbstractContainerScreenMixin で制限済み

### 3. **ClickType.QUICK_MOVE**
- **操作**: Shift + クリック
- **動作**: アイテムを別のインベントリに高速移動
- **対応**: 既存の AbstractContainerScreenMixin で制限済み

### 4. **ClickType.THROW**
- **操作**: Qキー（button=0）または Ctrl+Q（button=1）
- **動作**: アイテムを投げ捨てる
- **問題**: ロールスロットのアイテムを捨てられる可能性
- **対応**: クライアント側の制限を確認、必要に応じて追加制限

### 5. **ClickType.QUICK_CRAFT**
- **操作**: マウスドラッグでの複数スロット操作
- **動作**: 複数のスロットに一度にアイテムを配置
- **対応**: 既存の AbstractContainerScreenMixin で制限されている可能性あり

### 6. **ClickType.PICKUP_ALL**
- **操作**: ダブルクリック
- **動作**: 同じアイテムを集める
- **対応**: 既存の AbstractContainerScreenMixin で制限されている可能性あり

### 7. **ClickType.CLONE**
- **操作**: クリエイティブモードでの中クリック
- **動作**: アイテムをクローン
- **対応**: 通常プレイでは無関係、クリエイティブモードでは追加制限が必要かも

## 優先度

### 高
- **SWAP**: 数字キーでのホットバー切り替えは頻繁に使用される操作

### 中
- **THROW**: ロールスロットのアイテムを捨てられる可能性があるため確認必要

### 低
- **CLONE**: クリエイティブモード専用
- その他: 既存の制限で対応済みの可能性が高い

## 実装計画

### Phase 1: SWAP対応（必須）
1. AbstractContainerMenu.clicked() への Mixin 作成
2. ClickType.SWAP かつ slotId が 1-3 の場合を検出
3. RoleSlotBarrier を一時的に除去してスワップ実行
4. スワップ後に RoleSlotBarrier を復元

### Phase 2: THROW確認（推奨）
1. 現在の実装で THROW が制限されているか確認
2. 必要に応じて追加制限

### Phase 3: その他の確認（オプション）
1. QUICK_CRAFT, PICKUP_ALL などが正しく制限されているか確認
2. クリエイティブモードでの動作確認
