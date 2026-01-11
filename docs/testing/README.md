# Testing Documentation

One Slot Survival MODのテストドキュメント集です。

## ドキュメント一覧

### [gametest-guide.md](./gametest-guide.md) - GameTest実行ガイド ⭐推奨

**WorldOptions機能の自動テスト**

- runGameTestServerでの自動テスト実行
- 8つのテストケース（BonusItem配布、RoleChanging、DefaultModValidity等）
- CI/CD統合例
- トラブルシューティング

**クイックスタート**:
```bash
./gradlew runGameTestServer
```

---

### [world-options-testing.md](./world-options-testing.md) - 手動テストガイド

**WorldOptions機能の手動テスト**

- runClientでの手動テスト手順
- 6つのテストシナリオ
- テストコマンド集
- データ永続化確認

**クイックスタート**:
```bash
./gradlew runClient
# ゲーム内で: /osw bonusItem bundle
```

---

## テスト戦略

### 自動テスト（GameTest） - CI/CDに最適

✅ メリット：
- 高速（数秒で完了）
- 再現性が高い
- CI/CDに統合できる
- 回帰テスト向き

❌ デメリット：
- 実際のプレイ体験は確認できない
- 複雑なシナリオは実装が難しい

**用途**: 機能の正確性を検証、リグレッション防止

---

### 手動テスト - 実機確認に最適

✅ メリット：
- 実際のゲームプレイで確認できる
- UI/UXの確認が可能
- 複雑なシナリオも柔軟に対応

❌ デメリット：
- 時間がかかる
- 手順の再現性が低い
- CI/CDには不向き

**用途**: 新機能の動作確認、ユーザー体験の検証

---

## 推奨テストフロー

### 開発フェーズ

```
1. コード実装
   ↓
2. 自動テスト実行（GameTest）
   ./gradlew runGameTestServer
   ↓
3. 手動テスト（必要に応じて）
   ./gradlew runClient
```

### デプロイ前

```
1. 全自動テストを実行
   ./gradlew runGameTestServer
   ↓
2. 重要なシナリオを手動テスト
   ↓
3. CI/CDパイプラインで自動テスト
   ↓
4. リリース
```

---

## テスト対象機能

| 機能 | 自動テスト | 手動テスト |
|------|----------|----------|
| BonusItem配布（初回ログイン） | ✅ | ✅ |
| BonusItem配布（リスポーン） | ❌ | ✅ |
| MainRoleChanging設定 | ✅ | ✅ |
| SubRoleChanging設定 | ✅ | ✅ |
| DefaultModValidity設定 | ✅ | ✅ |
| 設定の永続化 | ✅ | ✅ |
| コマンドのエラー処理 | ❌ | ✅ |
| マルチプレイヤー動作 | ❌ | ✅ |

---

## 関連ファイル

- [WorldOptionsGameTests.java](../../src/main/java/com/github/godhexagon/oneslotsurvival/gametest/WorldOptionsGameTests.java) - 自動テスト実装
- [WorldOptions.java](../../src/main/java/com/github/godhexagon/oneslotsurvival/world/storage/WorldOptions.java) - テスト対象クラス
- [WorldEvent.java](../../src/main/java/com/github/godhexagon/oneslotsurvival/world/event/WorldEvent.java) - イベントハンドラ
- [test-commands-worldoptions.txt](./test-commands-worldoptions.txt) - 手動テスト用コマンド集
