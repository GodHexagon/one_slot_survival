# CommandDispatcher実行環境調査

## 調査目的

CommandDispatcherで登録されたコマンドがクライアントとサーバーのどちらで実行されるのかを明確にする。
また、`RegisterCommandsEvent`がどちらの環境で発火するのかを確認する。

## Web調査結果

### RegisterCommandsEvent

- **発火タイミング**: `Commands`クラスが構築されるたびに発火（`MinecraftForge.EVENT_BUS`）
- **発火場所**: サーバーサイド
- **Environment判定**: `getCommandSelection()`メソッドで環境を判別可能
  - `Commands.CommandSelection.INTEGRATED` - 統合サーバー（シングルプレイ）
  - `Commands.CommandSelection.DEDICATED` - 専用サーバー
  - `Commands.CommandSelection.ALL` - すべての環境

### RegisterClientCommandsEvent

- **用途**: クライアント専用コマンドの登録
- **発火場所**: クライアントサイドのみ
- **特徴**:
  - `CommandSourceStack`の代わりに`ISuggestionProvider`を使用
  - `world`と`server`は`null`
  - サーバーと通信せずにクライアントローカルで実行

### CommandDispatcher

- **基本動作**: Brigadierライブラリベースのコマンドシステム
- **実行環境**: サーバーサイド
- **コマンド優先順位**: 同名コマンドがクライアントとサーバーに存在する場合、クライアント側が優先される

### 参考情報源

- [MinecraftForge PR #6670 - Implement command for client side execution](https://github.com/MinecraftForge/MinecraftForge/pull/6670)
- [MinecraftForge PR #6952 - Allow command to be registered for client side execution](https://github.com/MinecraftForge/MinecraftForge/pull/6952)
- [RegisterCommandsEvent JavaDoc (1.19.3)](https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.19.3/net/minecraftforge/event/RegisterCommandsEvent.html)
- [Forge Community Wiki - Server and Client Commands](https://deepwiki.com/MinecraftForge/MinecraftForge/10.1-server-and-client-commands)

## 実装による検証

### 検証コード

`CommandEvent.java`に以下の検証コードを実装：

#### 1. イベント発火タイミングの検証

```java
@SubscribeEvent
public static void onRegisterCommands(RegisterCommandsEvent event) {
    String threadName = Thread.currentThread().getName();
    Commands.CommandSelection env = event.getCommandSelection();

    LOGGER.info("=== RegisterCommandsEvent fired ===");
    LOGGER.info("Thread: {}", threadName);
    LOGGER.info("Environment: {}", env);
    LOGGER.info("===================================");
}
```

#### 2. コマンド実行環境の検証

```java
event.getDispatcher().register(
    Commands.literal("testside")
        .executes(context -> {
            CommandSourceStack source = context.getSource();
            String threadName = Thread.currentThread().getName();
            boolean isClientSide = source.getLevel().isClientSide();
            String playerName = source.getEntity() != null ?
                source.getEntity().getName().getString() : "N/A";

            LOGGER.info("=== Command Execution ===");
            LOGGER.info("Thread: {}", threadName);
            LOGGER.info("isClientSide: {}", isClientSide);
            LOGGER.info("Player: {}", playerName);
            LOGGER.info("=========================");

            source.sendSuccess(() -> Component.literal(
                "Executed on: " + (isClientSide ? "CLIENT" : "SERVER") +
                "\nThread: " + threadName +
                "\nPlayer: " + playerName
            ), false);

            return 1;
        })
);
```

### 検証結果

#### RegisterCommandsEvent発火時（シングルプレイ起動時）

```
[20:40:32] [Worker-Main-7/INFO]: === RegisterCommandsEvent fired ===
[20:40:32] [Worker-Main-7/INFO]: Thread: Worker-Main-7
[20:40:32] [Worker-Main-7/INFO]: Environment: INTEGRATED
[20:40:32] [Worker-Main-7/INFO]: ===================================
```

**観察結果**:
- イベントはサーバー初期化スレッド（`Worker-Main-7`）で発火
- Environment値は`INTEGRATED`（統合サーバー = シングルプレイ）
- クライアントサイドでは発火していない

#### コマンド実行時（/testside実行）

```
[20:43:18] [Server thread/INFO]: === Command Execution ===
[20:43:18] [Server thread/INFO]: Thread: Server thread
[20:43:18] [Server thread/INFO]: isClientSide: false
[20:43:18] [Server thread/INFO]: Player: Dev
[20:43:18] [Server thread/INFO]: =========================
```

**観察結果**:
- コマンドはサーバーメインスレッド（`Server thread`）で実行
- `isClientSide`は`false`（サーバーサイド）
- プレイヤー情報にアクセス可能

## 結論

### RegisterCommandsEvent

✅ **サーバーサイド専用イベント**
- クライアントでは発火しない
- シングルプレイでもサーバースレッドで発火
- `getCommandSelection()`でINTEGRATED/DEDICATED/ALLを判定可能

### CommandDispatcher登録コマンド

✅ **サーバーサイドで実行**
- `isClientSide: false`
- サーバーメインスレッドで動作
- ワールド状態やエンティティに直接アクセス可能

### クライアントサイドコマンドが必要な場合

`RegisterClientCommandsEvent`を使用する必要がある：
- UI操作専用コマンド
- クライアント設定変更コマンド
- レンダリング関連コマンド
- サーバーと通信不要なローカル機能

### 実装上の注意点

1. **サーバーサイド前提で設計**
   - `RegisterCommandsEvent`で登録されるコマンドは常にサーバーで実行される
   - クライアントからコマンドを送信→サーバーで処理→結果をクライアントに送信

2. **Environment判定**
   ```java
   if (event.getCommandSelection() == Commands.CommandSelection.DEDICATED) {
       // 専用サーバー専用コマンド
   }
   ```

3. **クライアント専用機能との分離**
   - サーバーロジック: `RegisterCommandsEvent`
   - クライアント専用: `RegisterClientCommandsEvent`

## 検証環境

- Minecraft: 1.21.8
- Forge: 58.1.0
- Java: 21
- テスト環境: runClient（統合サーバー/シングルプレイ）

## 次のステップ

- [ ] 専用サーバー（`./gradlew runServer`）での動作確認
- [ ] `RegisterClientCommandsEvent`の実装例調査
- [ ] マルチプレイ環境での動作確認
