# GameTest実行ガイド

WorldOptions機能の自動テスト（GameTest）の実行方法を説明します。

## 概要

[WorldOptionsGameTests.java](../../src/main/java/com/github/godhexagon/oneslotsurvival/gametest/WorldOptionsGameTests.java)には、以下のテストが実装されています：

### 実装済みテスト一覧

| テスト名 | テスト内容 |
|---------|----------|
| `testBonusItemBundle` | Bundle配布（初回ログイン時） |
| `testBonusItemShulkerBox` | ShulkerBox配布（初回ログイン時） |
| `testBonusItemNone` | ボーナスアイテムなし |
| `testMainRoleChangingSetting` | MainRoleChanging設定の永続化 |
| `testSubRoleChangingSetting` | SubRoleChanging設定の永続化 |
| `testDefaultModValidityEnabled` | DefaultModValidity有効時の動作 |
| `testDefaultModValidityDisabled` | DefaultModValidity無効時の動作 |
| `testBonusItemPersistence` | BonusItem設定の永続化 |

**合計: 8テスト**

---

## テスト実行方法

### 方法1: runGameTestServer（推奨）

すべてのテストを自動実行します。

```bash
./gradlew runGameTestServer
```

#### 実行結果の見方

```
[Server thread/INFO]: Starting gametests...
[Server thread/INFO]: PASSED: oneslotsurvival:testbonusitembundle
[Server thread/INFO]: PASSED: oneslotsurvival:testbonusitemshulkerbox
[Server thread/INFO]: PASSED: oneslotsurvival:testbonusitemnone
[Server thread/INFO]: PASSED: oneslotsurvival:testmainrolechangingsetting
[Server thread/INFO]: PASSED: oneslotsurvival:testsubrolechangingsetting
[Server thread/INFO]: PASSED: oneslotsurvival:testdefaultmodvalidityenabled
[Server thread/INFO]: PASSED: oneslotsurvival:testdefaultmodvaliditydisabled
[Server thread/INFO]: PASSED: oneslotsurvival:testbonusitempersistence
[Server thread/INFO]: 8 tests passed, 0 tests failed
```

**終了コード**:
- `0`: すべてのテストが成功
- `n` (n > 0): n個のテストが失敗

---

### 方法2: ゲーム内でテスト実行

個別のテストをデバッグしたい場合に便利です。

#### 手順

1. **クライアントを起動**
   ```bash
   ./gradlew runClient
   ```

2. **ワールドを作成**
   - シングルプレイ → 新しいワールドを作成
   - チートを許可: ON

3. **ゲーム内でテストコマンドを実行**

   ```
   # すべてのテストを実行
   /test runall

   # 個別のテストを実行
   /test run oneslotsurvival:testbonusitembundle

   # 失敗したテストだけ再実行
   /test runfailed

   # テスト結果を表示
   /test exportthisrun
   ```

#### デバッグに便利なコマンド

```
# 利用可能なテスト一覧を表示
/test list

# テスト実行中の可視化
/gamerule showGameTestMarkers true

# テストエリアをクリア
/test cleartests
```

---

## CI/CD統合

### GitHub Actions での使用例

```yaml
name: Run GameTests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Run GameTests
        run: ./gradlew runGameTestServer
```

終了コードが非ゼロの場合、CIが失敗します。

---

## テストの仕組み

### 1. テスト構造

各テストメソッドは以下の構造を持ちます：

```java
@GameTest(
    setupTicks = 20,      // セットアップ時間（ティック）
    maxTicks = 100,       // タイムアウト時間（ティック）
    required = true       // 失敗時にバッチを停止するか
)
public static void testBonusItemBundle(GameTestHelper helper) {
    // テスト実装
    helper.succeed();  // 成功
}
```

### 2. プレイヤーシミュレーション

`simulateFirstLogin(player)` メソッドで、[WorldEvent.java:100-134](../../src/main/java/com/github/godhexagon/oneslotsurvival/world/event/WorldEvent.java#L100-L134) の初回ログイン処理を再現しています。

```java
private static void simulateFirstLogin(ServerPlayer player) {
    // PLAY_TIME < 5 の場合の処理を再現
    MainRole currentRoleId = RoleManager.getMainRole(player);
    if (currentRoleId == MainRole.UNASSIGNED) {
        // MOD有効性を設定
        boolean defaultModValidity = WorldOptions.isDefaultModValidityEnabled(player.getServer());
        PlayerModValidity.setEnabled(player, defaultModValidity);

        // デフォルトロールを設定
        RoleManager.setMainRole(player, MainRole.MINER);
        RoleManager.setSubRole(player, SubRole.FISHER);

        // ボーナスアイテムを配布
        BonusItem bonusItemMode = WorldOptions.getBonusItem(player.getServer());
        if (bonusItemMode == BonusItem.BUNDLE || bonusItemMode == BonusItem.BUNDLE_RESPAWN) {
            player.addItem(new ItemStack(Items.BUNDLE));
        } else if (bonusItemMode == BonusItem.SHULKERBOX || bonusItemMode == BonusItem.SHULKERBOX_RESPAWN) {
            player.addItem(new ItemStack(Items.SHULKER_BOX));
        }
    }
}
```

### 3. アサーション

インベントリのアイテム数を確認：

```java
long bundleCount = countItem(inventory, Items.BUNDLE);

if (bundleCount == 1) {
    helper.succeed();
} else {
    helper.fail("Expected 1 bundle in inventory, but found " + bundleCount);
}
```

---

## テスト追加方法

新しいテストを追加する場合：

1. **[WorldOptionsGameTests.java](../../src/main/java/com/github/godhexagon/oneslotsurvival/gametest/WorldOptionsGameTests.java) にメソッドを追加**

   ```java
   @GameTest(
       setupTicks = 20,
       maxTicks = 100,
       required = true
   )
   public static void testNewFeature(GameTestHelper helper) {
       // テスト実装
       helper.succeed();
   }
   ```

2. **再コンパイル**

   ```bash
   ./gradlew compileJava
   ```

3. **テスト実行**

   ```bash
   ./gradlew runGameTestServer
   ```

---

## トラブルシューティング

### テストが見つからない

**症状**: `/test list` で何も表示されない

**原因**: GameTestが正しく登録されていない

**解決策**:
1. `build.gradle` で `forge.enabledGameTestNamespaces` が設定されているか確認
   ```gradle
   gameTestServer {
       property 'forge.enabledGameTestNamespaces', mod_id
   }
   ```

2. クラスに `@GameTestNamespace("oneslotsurvival")` が付いているか確認

3. 再ビルド
   ```bash
   ./gradlew build --refresh-dependencies
   ```

### テストがタイムアウトする

**症状**: `Test timed out after 100 ticks`

**原因**: `helper.succeed()` が呼ばれていない

**解決策**:
1. `maxTicks` を増やす
2. 遅延処理で `helper.succeed()` を呼ぶ
   ```java
   helper.runAfterDelay(50, () -> {
       // 検証
       helper.succeed();
   });
   ```

### プレイヤーが生成されない

**症状**: `makeMockPlayer` が失敗する

**原因**: GameTestHelper の制限

**解決策**:
現在の実装では `helper.makeMockPlayer()` でモックプレイヤーを生成していますが、
サーバー環境によっては動作しない場合があります。

代替案：
- テスト用のダミーServerPlayerを手動で作成
- または、構造テンプレートにアーマースタンドを配置してプレイヤーの代わりに使用

---

## 参考資料

- [Forge GameTest Documentation](https://docs.minecraftforge.net/en/1.18.x/misc/gametest/)
- [Minecraft Wiki - GameTest](https://minecraft.wiki/w/GameTest)
- [WorldOptionsGameTests.java](../../src/main/java/com/github/godhexagon/oneslotsurvival/gametest/WorldOptionsGameTests.java)

---

## まとめ

WorldOptions機能の自動テストは以下のコマンドで実行できます：

```bash
# すべてのテストを自動実行
./gradlew runGameTestServer

# ゲーム内で個別に実行
./gradlew runClient
# ゲーム内で: /test runall
```

8つのテストがすべて成功すれば、WorldOptions機能が正しく動作していることが確認できます。
