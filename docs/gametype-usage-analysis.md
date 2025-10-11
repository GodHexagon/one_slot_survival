# GameType使用箇所の機能別分類（Minecraft Forge 1.21.8）

このドキュメントは、バニラMinecraft及びForgeにおける`GameType`の使用箇所を網羅的に調査し、機能別に分類したものです。

## GameType クラスの基本情報

- **パッケージ**: `net.minecraft.world.level`
- **型**: enum（列挙型）
- **enum定数**:
  - `SURVIVAL(0, "survival")`
  - `CREATIVE(1, "creative")`
  - `ADVENTURE(2, "adventure")`
  - `SPECTATOR(3, "spectator")`
- **デフォルト値**: `GameType.DEFAULT_MODE = SURVIVAL`

### 主要メソッド:
- **識別子取得**: `getId()`, `getName()`, `getSerializedName()`
- **表示名取得**: `getLongDisplayName()`, `getShortDisplayName()`
- **能力更新**: `updatePlayerAbilities(Abilities)`
- **判定メソッド**: `isCreative()`, `isSurvival()`, `isBlockPlacingRestricted()`
- **変換メソッド**: `byId(int)`, `byName(String)`, `byNullableId(int)`, `getNullableId(GameType)`
- **検証メソッド**: `isValidId(int)`

### コーデック:
- `CODEC` (StringRepresentable.EnumCodec)
- `STREAM_CODEC` (ByteBuf用)
- `LEGACY_ID_CODEC` (旧形式、非推奨)

---

## 機能分類

### 1. プレイヤー能力制御（Player Abilities Management）
**目的**: GameTypeに応じてプレイヤーの能力（飛行、無敵、即座に破壊など）を設定・更新する

**使用箇所**:
- `net.minecraft.world.level.GameType.updatePlayerAbilities(Abilities)` - GameTypeに基づいて能力を設定（CREATIVE: 飛行+即座に破壊+無敵、SPECTATOR: 飛行+無敵、その他: 通常）
- `net.minecraft.server.level.ServerPlayerGameMode.setGameModeForPlayer(GameType, GameType)` - ゲームモード変更時にプレイヤー能力を更新
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.adjustPlayer(Player)` - クライアント側でGameTypeに応じた能力を適用
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.setLocalMode(GameType, GameType)` - クライアントのローカルモード設定と能力更新

### 2. サーバー側ゲームモード管理（Server-side Game Mode Management）
**目的**: サーバー側でプレイヤーのゲームモードを管理・変更する

**使用箇所**:
- `net.minecraft.server.level.ServerPlayerGameMode.gameModeForPlayer` (フィールド) - プレイヤーの現在のゲームモード
- `net.minecraft.server.level.ServerPlayerGameMode.previousGameModeForPlayer` (フィールド) - 前回のゲームモード（@Nullable）
- `net.minecraft.server.level.ServerPlayerGameMode.changeGameModeForPlayer(GameType)` - ゲームモードを変更し、変更があれば他プレイヤーに通知
- `net.minecraft.server.level.ServerPlayerGameMode.getGameModeForPlayer()` - 現在のゲームモードを取得
- `net.minecraft.server.level.ServerPlayerGameMode.getPreviousGameModeForPlayer()` - 前回のゲームモードを取得
- `net.minecraft.server.level.ServerPlayerGameMode.isSurvival()` - サバイバル系モードか判定
- `net.minecraft.server.level.ServerPlayerGameMode.isCreative()` - クリエイティブモードか判定

### 3. クライアント側ゲームモード管理（Client-side Game Mode Management）
**目的**: クライアント側でローカルプレイヤーのゲームモードを管理する

**使用箇所**:
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.localPlayerMode` (フィールド) - クライアント側のゲームモード
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.previousLocalPlayerMode` (フィールド) - 前回のゲームモード
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.setLocalMode(GameType)` - ローカルモードを設定
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.canHurtPlayer()` - プレイヤーがダメージを受けるか判定
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.getPreviousPlayerMode()` - 前回のモードを取得
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.getPlayerMode()` - 現在のモードを取得
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.hasExperience()` - 経験値を持つか判定
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.hasMissTime()` - 採掘時間が必要か判定
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.isAlwaysFlying()` - 常に飛行モードか判定

### 4. ブロック破壊・設置制御（Block Breaking and Placement Control）
**目的**: GameTypeに応じてブロックの破壊・設置の可否を制御する

**使用箇所**:
- `net.minecraft.world.level.GameType.isBlockPlacingRestricted()` - ブロック設置が制限されているか（ADVENTURE、SPECTATORで制限）
- `net.minecraft.server.level.ServerPlayerGameMode.handleBlockBreakAction(BlockPos, Action, Direction, int, int)` - ゲームモードに基づくブロック破壊処理
- `net.minecraft.server.level.ServerPlayerGameMode.destroyBlock(BlockPos)` - ゲームモードチェックを含むブロック破壊
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.destroyBlock(BlockPos)` - クライアント側ブロック破壊
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.startDestroyBlock(BlockPos, Direction)` - ブロック破壊開始
- `net.minecraft.world.entity.player.Player.blockActionRestricted(Level, BlockPos, GameType)` - ブロックアクション制限の判定

### 5. アイテム使用制御（Item Usage Control）
**目的**: GameTypeに応じてアイテムの使用を制御する

**使用箇所**:
- `net.minecraft.server.level.ServerPlayerGameMode.useItem(ServerPlayer, Level, ItemStack, InteractionHand)` - SPECTATORモードでは使用不可
- `net.minecraft.server.level.ServerPlayerGameMode.useItemOn(ServerPlayer, Level, ItemStack, InteractionHand, BlockHitResult)` - SPECTATORモードでは特別処理
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.useItem(Player, InteractionHand)` - SPECTATORモードでは使用不可
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.performUseItemOn(LocalPlayer, InteractionHand, BlockHitResult)` - SPECTATORモードでは消費のみ

### 6. エンティティ相互作用制御（Entity Interaction Control）
**目的**: GameTypeに応じてエンティティとの相互作用を制御する

**使用箇所**:
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.attack(Player, Entity)` - SPECTATORモードでは攻撃不可
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.interact(Player, Entity, InteractionHand)` - SPECTATORモードでは相互作用不可
- `net.minecraft.client.multiplayer.MultiPlayerGameMode.interactAt(Player, Entity, EntityHitResult, InteractionHand)` - SPECTATORモードでは相互作用不可

### 7. プレイヤー情報管理（Player Info Management）
**目的**: プレイヤー情報（タブリスト等）でGameTypeを管理・表示する

**使用箇所**:
- `net.minecraft.client.multiplayer.PlayerInfo.gameMode` (フィールド) - プレイヤーのゲームモード情報
- `net.minecraft.client.multiplayer.PlayerInfo.getGameMode()` - ゲームモードを取得
- `net.minecraft.client.multiplayer.PlayerInfo.setGameMode(GameType)` - ゲームモードを設定（Forge event発火）

### 8. ネットワークパケット送受信（Network Packet Serialization）
**目的**: クライアント・サーバー間でGameTypeをパケットで送受信する

**使用箇所**:
- `net.minecraft.network.protocol.game.CommonPlayerSpawnInfo` - スポーン情報にGameTypeを含む（gameType, previousGameType）
  - コンストラクタ: `GameType.byId()`, `GameType.byNullableId()`でデシリアライズ
  - `write()`: `gameType.getId()`, `GameType.getNullableId()`でシリアライズ
- `net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket` - プレイヤー情報更新パケット（UPDATE_GAME_MODEアクション）
- `net.minecraft.network.protocol.game.ServerboundChangeGameModePacket` - ゲームモード変更リクエストパケット

### 9. ワールド設定・保存（World Settings and Persistence）
**目的**: ワールドのデフォルトゲームモードを設定・保存する

**使用箇所**:
- `net.minecraft.world.level.LevelSettings.gameType` (フィールド) - ワールドのゲームタイプ
- `net.minecraft.world.level.LevelSettings.gameType()` - ゲームタイプ取得
- `net.minecraft.world.level.LevelSettings.withGameType(GameType)` - ゲームタイプを変更した新しいLevelSettings作成
- `net.minecraft.world.level.LevelSettings.parse(Dynamic, WorldDataConfiguration)` - NBTからGameTypeをパース
- `net.minecraft.world.level.storage.PrimaryLevelData` - ワールドデータとしてLevelSettingsを保持
- `net.minecraft.world.level.storage.ServerLevelData` - サーバーレベルデータインターフェース
- `net.minecraft.world.level.storage.WorldData` - ワールドデータインターフェース
- `net.minecraft.world.level.storage.DerivedLevelData` - 派生レベルデータ

### 10. プレイヤーデータ永続化（Player Data Persistence）
**目的**: プレイヤーのゲームモードをNBTに保存・読み込みする

**使用箇所**:
- `net.minecraft.server.level.ServerPlayer.loadGameTypes(ValueInput)` - プレイヤーデータからGameTypeを読み込み
- `net.minecraft.server.level.ServerPlayer.readPlayerMode(ValueInput, String)` - `LEGACY_ID_CODEC`を使用してGameTypeを読み込み
- `net.minecraft.server.level.ServerPlayer.calculateGameModeForNewPlayer(GameType)` - 新規プレイヤーのGameType計算
- `net.minecraft.server.level.ServerPlayer.addAdditionalSaveData(CompoundTag)` - GameTypeをNBTに保存

### 11. サーバー設定管理（Server Configuration Management）
**目的**: サーバー全体のデフォルトゲームモード、強制ゲームモードを管理する

**使用箇所**:
- `net.minecraft.server.MinecraftServer.getDefaultGameType()` - デフォルトゲームタイプを取得
- `net.minecraft.server.MinecraftServer.setDefaultGameType(GameType)` - デフォルトゲームタイプを設定
- `net.minecraft.server.MinecraftServer.getForcedGameType()` - 強制ゲームタイプを取得（@Nullable）
- `net.minecraft.server.MinecraftServer.publishServer(GameType, boolean, int)` - サーバー公開時のゲームタイプ指定
- `net.minecraft.server.MinecraftServer.DEMO_SETTINGS` - デモワールド設定（GameType.SURVIVAL）
- `net.minecraft.server.dedicated.DedicatedServerProperties` - 専用サーバーのプロパティ設定

### 12. コマンド実装（Command Implementation）
**目的**: /gamemode, /defaultgamemodeコマンドでGameTypeを変更する

**使用箇所**:
- `net.minecraft.commands.arguments.GameModeArgument` - GameTypeの引数パーサー
  - `parse(StringReader)`: `GameType.byName()`で文字列をGameTypeに変換
  - `listSuggestions()`: すべてのGameType値を提案
  - `getGameMode(CommandContext, String)`: コマンドコンテキストからGameTypeを取得
- `net.minecraft.server.commands.GameModeCommand` - /gamemodeコマンド実装
  - `setMode()`: プレイヤーのGameTypeを変更
  - `setGameMode(ServerPlayer, GameType)`: ゲームモード設定
- `net.minecraft.server.commands.DefaultGameModeCommands` - /defaultgamemodeコマンド実装
  - `setMode(CommandSourceStack, GameType)`: サーバーのデフォルトGameTypeを設定

### 13. エンティティセレクタ（Entity Selector）
**目的**: コマンドのエンティティセレクタでGameTypeによるフィルタリングを行う

**使用箇所**:
- `net.minecraft.commands.arguments.selector.options.EntitySelectorOptions` - セレクタオプション登録

### 14. Advancements（進捗）判定（Advancement Criteria）
**目的**: 進捗の条件としてGameTypeを判定する

**使用箇所**:
- `net.minecraft.advancements.critereon.GameTypePredicate` - GameType述語クラス
  - `matches(GameType)`: GameTypeが条件に一致するか判定
  - `ANY`: すべてのGameTypeにマッチ
  - `SURVIVAL_LIKE`: SURVIVALとADVENTUREにマッチ
- `net.minecraft.advancements.critereon.PlayerPredicate.gameType` - プレイヤー述語の一部としてGameTypeを判定

### 15. UI表示（User Interface Display）
**目的**: クライアントUIでGameTypeを表示・選択する

**使用箇所**:
- `net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen` - F3+Fゲームモード切り替え画面
  - `GameModeIcon`: 各GameTypeのアイコン定義（CREATIVE, SURVIVAL, ADVENTURE, SPECTATOR）
  - `getFromGameType(GameType)`: GameTypeからアイコンを取得
  - `switchToHoveredGameMode()`: 選択したGameTypeに切り替え
- `net.minecraft.client.gui.screens.worldselection.CreateWorldScreen` - ワールド作成画面
- `net.minecraft.client.gui.screens.worldselection.WorldCreationUiState` - ワールド作成UI状態管理
  - `SelectedGameMode`: UI用GameMode enum（SURVIVAL, HARDCORE, CREATIVE, DEBUG）
  - 各値はGameTypeと対応
- `net.minecraft.client.gui.screens.ShareToLanScreen` - LANに公開画面

### 16. ワールドリスト表示（World List Display）
**目的**: ワールド選択画面でワールドのGameTypeを表示する

**使用箇所**:
- `net.minecraft.world.level.storage.LevelSummary.getGameMode()` - ワールドサマリーからGameTypeを取得
- `net.minecraft.client.gui.screens.worldselection.WorldSelectionList` - ワールドリスト表示

### 17. プレイヤー判定メソッド（Player Type Checking）
**目的**: プレイヤークラス内でGameTypeに基づく判定を行う

**使用箇所**:
- `net.minecraft.world.entity.player.Player.gameMode()` - 抽象メソッド、プレイヤーのGameTypeを返す
- `net.minecraft.world.entity.player.Player.isSpectator()` - `gameMode() == GameType.SPECTATOR`で判定
- `net.minecraft.world.entity.player.Player.isCreative()` - `gameMode() == GameType.CREATIVE`で判定
- `net.minecraft.world.entity.player.Player.blockActionRestricted(Level, BlockPos, GameType)` - GameTypeに基づくブロックアクション制限判定

### 18. サーバープレイヤー実装（Server Player Implementation）
**目的**: ServerPlayerクラスでGameType関連メソッドを実装する

**使用箇所**:
- `net.minecraft.server.level.ServerPlayer.gameMode()` - `gameMode.getGameModeForPlayer()`を返す
- `net.minecraft.server.level.ServerPlayer.setGameMode(GameType)` - ゲームモードを変更し、イベント処理を行う

### 19. クイックプレイログ（Quick Play Logging）
**目的**: クイックプレイ機能でGameTypeをログに記録する

**使用箇所**:
- `net.minecraft.client.quickplay.QuickPlayLog` - クイックプレイログ

### 20. テレメトリー（Telemetry）
**目的**: ゲームプレイデータとしてGameTypeを収集する

**使用箇所**:
- `net.minecraft.client.telemetry.events.WorldLoadEvent` - ワールド読み込みイベント
- `net.minecraft.client.telemetry.WorldSessionTelemetryManager` - ワールドセッションテレメトリ

### 21. チュートリアル（Tutorial）
**目的**: チュートリアルでGameTypeに応じた処理を行う

**使用箇所**:
- `net.minecraft.client.tutorial.Tutorial` - チュートリアルシステム

### 22. スペクテイター専用UI（Spectator-specific UI）
**目的**: スペクテイターモード専用のUI機能を提供する

**使用箇所**:
- `net.minecraft.client.gui.spectator.categories.TeleportToPlayerMenuCategory` - プレイヤーへのテレポートメニュー
- `net.minecraft.client.gui.spectator.categories.TeleportToTeamMenuCategory` - チームへのテレポートメニュー

### 23. レンダリング（Rendering）
**目的**: GameTypeに応じたレンダリング処理を行う

**使用箇所**:
- `net.minecraft.client.renderer.GameRenderer` - ゲームレンダラー
- `net.minecraft.client.gui.Gui` - HUD表示
- `net.minecraft.client.gui.components.PlayerTabOverlay` - プレイヤーリストオーバーレイ

### 24. クライアント入力処理（Client Input Handling）
**目的**: GameTypeに応じた入力処理を行う

**使用箇所**:
- `net.minecraft.client.KeyboardHandler` - キーボード入力（F3+Fでゲームモード切り替え）
- `net.minecraft.client.player.LocalPlayer` - ローカルプレイヤー入力処理
- `net.minecraft.client.player.AbstractClientPlayer` - 抽象クライアントプレイヤー

### 25. ワールド/レベル管理（World/Level Management）
**目的**: レベル（ディメンション）でGameTypeを管理する

**使用箇所**:
- `net.minecraft.server.level.ServerLevel` - サーバーレベル
- `net.minecraft.client.multiplayer.ClientLevel` - クライアントレベル

### 26. パケットリスナー（Packet Listener）
**目的**: ネットワークパケットを受信してGameType関連処理を行う

**使用箇所**:
- `net.minecraft.client.multiplayer.ClientPacketListener` - クライアントパケットリスナー
- `net.minecraft.server.network.ServerGamePacketListenerImpl` - サーバーゲームパケットリスナー

### 27. プレイヤーリスト管理（Player List Management）
**目的**: サーバーのプレイヤーリストでGameTypeを管理する

**使用箇所**:
- `net.minecraft.server.players.PlayerList.placeNewPlayer()` - 新規プレイヤー参加時の処理

### 28. 統合サーバー（Integrated Server）
**目的**: シングルプレイ用統合サーバーでGameTypeを管理する

**使用箇所**:
- `net.minecraft.client.server.IntegratedServer` - 統合サーバー
- `net.minecraft.server.commands.PublishCommand` - LANに公開コマンド

### 29. 専用サーバー（Dedicated Server）
**目的**: 専用サーバーでGameTypeを管理する

**使用箇所**:
- `net.minecraft.server.dedicated.DedicatedServer` - 専用サーバー
- `net.minecraft.server.dedicated.DedicatedServerProperties` - server.propertiesのゲームモード設定

### 30. ゲームテスト（Game Test）
**目的**: ゲームテストフレームワークでGameTypeを使用する

**使用箇所**:
- `net.minecraft.gametest.framework.GameTestHelper` - ゲームテストヘルパー
- `net.minecraft.gametest.framework.GameTestServer` - ゲームテストサーバー

### 31. Realmsクライアント（Realms Client）
**目的**: Minecraft RealmsでGameTypeを管理する

**使用箇所**:
- `com.mojang.realmsclient.dto.RealmsWorldOptions` - Realmsワールドオプション
- `com.mojang.realmsclient.gui.screens.configuration.RealmsSlotOptionsScreen` - Realmsスロットオプション画面
- `com.mojang.realmsclient.RealmsMainScreen` - Realmsメイン画面

### 32. Forge イベント（Forge Events）
**目的**: Forge独自のイベントシステムでGameType変更を通知する

**使用箇所**:
- `net.minecraftforge.client.event.ClientPlayerChangeGameTypeEvent` - クライアント側GameType変更イベント
- `net.minecraftforge.event.entity.player.PlayerEvent` - プレイヤーイベント
- `net.minecraftforge.client.ForgeHooksClient.onClientChangeGameType()` - クライアントGameType変更フック
- `net.minecraftforge.common.ForgeHooks.onBlockBreakEvent()` - ブロック破壊イベント（GameType引数）

### 33. Forge GUI オーバーレイ（Forge GUI Overlay）
**目的**: Forge のGUIオーバーレイでGameTypeを使用する

**使用箇所**:
- `net.minecraftforge.client.gui.overlay.VanillaGuiOverlay` - バニラGUIオーバーレイ

---

## 統計

- **総使用ファイル数**: 57ファイル
- **機能分類数**: 33個
- **主要な使用パターン**:
  1. **判定・条件分岐**: `if (gameType == GameType.SPECTATOR)` のようなGameType比較
  2. **能力制御**: `gameType.updatePlayerAbilities()` による能力更新
  3. **シリアライゼーション**: `GameType.byId()`, `getId()` によるID変換
  4. **文字列変換**: `GameType.byName()`, `getName()` による名前変換
  5. **Nullable処理**: `GameType.byNullableId()`, `getNullableId()` による前回のゲームモード管理

### 使用頻度の高いメソッド:
1. `getId()` / `byId(int)` - ネットワーク送信、NBT保存で頻繁に使用
2. `updatePlayerAbilities(Abilities)` - ゲームモード変更時に必ず呼ばれる
3. `getName()` / `byName(String)` - コマンド、UI表示で使用
4. `isCreative()`, `isSurvival()`, `isBlockPlacingRestricted()` - 条件判定で頻繁に使用
5. `byNullableId(int)` / `getNullableId(GameType)` - 前回のゲームモード管理

### アーキテクチャ的特徴:
- **サーバー・クライアント分離**: ServerPlayerGameMode（サーバー）とMultiPlayerGameMode（クライアント）で別々に管理
- **不変性**: GameTypeは enum なので不変、変更時は新しい値を設定
- **レガシーサポート**: LEGACY_ID_CODEC で旧バージョンとの互換性を維持
- **拡張性**: Forge イベントシステムで GameType 変更を検知可能

---

## MOD開発への示唆

### GameTypeと同様の「MOD有効性」パラメーターを実装する場合の参考ポイント

#### 1. データ構造
- **enum型の活用**: GameTypeはenumなので、MOD有効性も`boolean`ではなく`enum ModValidity { ENABLED, DISABLED }`として実装可能
- **前回の状態管理**: GameTypeは`previousGameMode`を持つ。MOD有効性でも前回の状態を管理するか検討

#### 2. 管理クラスの設計
- **サーバー側**: `ServerPlayerGameMode`相当のクラスが必要
- **クライアント側**: `MultiPlayerGameMode`相当のクラスが必要
- **分離の理由**: サーバーが権威、クライアントは表示用

#### 3. 永続化
- **プレイヤーデータ**: `ServerPlayer.addAdditionalSaveData()`でNBTに保存
- **ワールドデータ**: `LevelSettings`相当の場所に保存
- **コーデック**: `CODEC`と`LEGACY_ID_CODEC`の両方を用意（互換性のため）

#### 4. ネットワーク同期
- **パケット定義**: `CommonPlayerSpawnInfo`のようにスポーン時に同期
- **更新パケット**: `ClientboundPlayerInfoUpdatePacket`のように変更時に同期
- **前回の状態**: Nullable型で前回の状態も送信

#### 5. 能力制御
- **中央管理**: `GameType.updatePlayerAbilities()`のように、1つのメソッドで能力を制御
- **モード変更時**: 必ず能力を更新

#### 6. イベント統合
- **Forgeイベント**: `ClientPlayerChangeGameTypeEvent`相当のイベントを発火
- **フック**: `ForgeHooks`で他MODとの統合ポイントを提供

#### 7. UI統合
- **デバッグ画面**: F3+Fのようなトグル機能
- **コマンド**: `/gamemode`相当のコマンド実装
- **タブリスト**: `PlayerInfo`に情報を追加
