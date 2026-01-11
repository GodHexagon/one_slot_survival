# One Slot Survival

インベントリの制限という縛りプレイを楽しむ上級者向けMinecraft Forge MOD。

※マルチプレイサーバー対応。必ずサーバーとクライアント両方に、当MODをForge MODとして導入してください。

## 動作環境

- Java 21
- Minecraft Java Edition 1.21.8
- Forge 58.1.0

## 主な特徴

- **１スロット縛り**: インベントリが基本1スロット（メインハンド）+ オフハンド + 装備スロットのみに。マイルストーン（ダイヤモンド、ネザーゲート、エンドラ討伐など）の難易度を上げてMinecraftを楽しみ直せます。
- **協力プレイ促進**: 4つのメインロールと7つのセカンダリロールを、プレイヤーは各一つずつ選択可能。それぞれの能力を活かす立ち回りを目指しましょう。
- **成長要素**: プレイ行動でレベルアップします。これにより、インベントリスロットを獲得していきます。インベントリスロットには種類があり、ロールによって異なる種類を獲得します。

### 注意事項

当MODは縛りプレイOnlyです。レベルが上がると少し便利になりますがバニラの状態より強化されることはありません。

## 遊び方

ゲームを開始すると、すぐにインベントリが１スロットのみに制限されるはずです。

通常、プレイヤーは以下のコマンドを利用可能です。

- `/oneslot`: 自身のロールとレベルアップ状態を確認。
- `/oneslot changerole`, `/oneslot changerole <ロール名>`: 自身に割り当てられたロールの変更を試みる。これにより、レベルアップで獲得できるスロットの種類を変えることができます。

管理者向けコマンドもあります。すべてのコマンドの一覧は、「[コマンド](#コマンド)」の項を参照してください。

## コマンド

### 一般プレイヤー向け
- `/oneslot` - 自身のメインロール・サブロール、レベル、次のレベルまでの必要経験値を表示
- `/oneslot status` - `/oneslot` のエイリアス
- `/oneslot next` - 次のレベルアップで獲得できる報酬を表示
- `/oneslot changerole` - 選択可能なロール一覧を表示
- `/oneslot changerole <ロール名>` - 指定ロールへの変更内容を確認（例: `/oneslot changerole warrior`）
- `/oneslot changerole <ロール名> agree` - ロール変更を実行
- `/oneslot data` - 全ロールの一覧を表示
- `/oneslot data <ロール名>` - 指定ロールの詳細を表示
- `/oneslot data <ロール名> slot <スロット番号>` - 指定ロールの特定スロットの詳細を表示

### サーバー管理者向け - プレイヤー個別コマンド（OP権限必要）

**OP権限レベル2が必要。コマンドエイリアス: `/osa` または `/oneslot admin`**

#### MOD有効性管理
- `/osa validity get [プレイヤー]` - MOD有効状態を確認
- `/osa validity enable [プレイヤー]` - MODを有効化
- `/osa validity disable [プレイヤー]` - MODを無効化
- `/osa validity toggle [プレイヤー]` - MOD有効状態を切り替え

#### ロール管理
- `/osa role get [プレイヤー]` - ロールを確認
- `/osa role set <ロール名> [プレイヤー]` - ロールを設定（進捗リセット）
- `/osa role setNoClear <ロール名> [プレイヤー]` - ロールを設定（進捗保持）
- `/osa role clear [プレイヤー]` - 全ロールをクリア
- `/osa role clear main [プレイヤー]` - メインロールをクリア
- `/osa role clear sub [プレイヤー]` - サブロールをクリア

#### レベル管理
- `/osa level get [プレイヤー]` - レベルを確認
- `/osa level set <レベル> [プレイヤー]` - レベルを設定（経験値リセット）
- `/osa level setNoClear <レベル> [プレイヤー]` - レベルを設定（経験値保持）
- `/osa level clear [プレイヤー]` - レベルをリセット

#### 経験値管理
- `/osa xp getRemaining [プレイヤー]` - 次のレベルまでの必要経験値を確認
- `/osa xp add <量> [プレイヤー]` - 経験値を付与
- `/osa xp clear [プレイヤー]` - 経験値をクリア

#### ロールレベルアップ回数管理
- `/osa roleLeveledUpTimes get [プレイヤー]` - ロールレベルアップ回数を確認
- `/osa roleLeveledUpTimes setMain <回数> [プレイヤー]` - メインロールレベルアップ回数を設定
- `/osa roleLeveledUpTimes setSub <回数> [プレイヤー]` - サブロールレベルアップ回数を設定
- `/osa roleLeveledUpTimes clear main [プレイヤー]` - メインロールレベルアップ回数をリセット
- `/osa roleLeveledUpTimes clear sub [プレイヤー]` - サブロールレベルアップ回数をリセット
- `/osa roleLeveledUpTimes clear both [プレイヤー]` - 両方のロールレベルアップ回数をリセット

#### ロール一括配布
- `/osa distributeRole unassign` - 全オンラインプレイヤーのロールを未割り当てに
- `/osa distributeRole random` - 全オンラインプレイヤーにランダムにロールを割り当て
- `/osa distributeRole definedList` - 定義済みリストに基づいてロールを順番に割り当て

**定義済みリスト**=
- メインロール: Miner, Warrior, Survivor, Builder
- サブロール: Armorer, Archer, Fisher, Breeder | Scholar, Pharmacist, Thrower

### サーバー管理者向け - ワールド設定コマンド（OP権限必要）

**OP権限レベル2が必要。コマンドエイリアス: `/osw` または `/oneslot world`**

- `/osw mainRoleChanging [true|false]` - メインロール変更の可否を設定・照会
- `/osw subRoleChanging [true|false]` - サブロール変更の可否を設定・照会
- `/osw bonusItem [none|bundle|shulkerbox|bundle_respawn|shulkerbox_respawn]` - ボーナスアイテムを設定・照会
- `/osw defaultModValidity [true|false]` - 新規プレイヤーのMOD初期有効状態を設定・照会
- `/osw defaultRole [unassign|random|definedList]` - 新規プレイヤーのロール初期割り当て方式を設定・照会　※definedListについては、「[ロール一括配布](#ロール一括配布)」と同様

**注**: `[プレイヤー]` は省略可能。省略時はコマンド実行者自身が対象になります。

## 開発について

**リポジトリ**: https://github.com/GodHexagon/one_slot_survival

**過去と未来の計画**: [リポジトリにあるタスクリスト](https://github.com/GodHexagon/one_slot_survival/blob/develop/docs/task_list.md)をご覧ください。

**問題報告**: [CurseForgeのコメント機能](https://www.curseforge.com/minecraft/mc-mods/one-slot-survival/comments)にてお寄せください。

**ライセンス**: Copyright (c) 2025 GodHexagon (とおる) All rights reserved. 

詳細は [LICENSE](https://github.com/GodHexagon/one_slot_survival/blob/develop/LICENSE.txt) ファイルを参照してください。
