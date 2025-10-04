# パッケージ構成計画

https://tree.nathanfriend.com/?s=(%27optiYs!(%27fancy!true~fullPath!false~trail_gSlash!true~rootDot!false)~source!(%27source!%27com.github.godhexagY.Yeslotsurvival0One6SurvivalModH0mix_0*wkJi7J*652952XPushable-*mobJ*Drop952XMob5cli3Ji7J*XCYta_erQ52I7652TooltipMix_H0ev30*ForgeWkq-ForgeCli3q-Drop9q-CYta_erQqH0cU0*p4JKCU-*6CU-adm_JwkJ*I7QCompatibility-2KChang_gZVGZKZ9s-*actiYJ*P4VG-2P4K-2P4Level-2KD80i7ui0*zcYfigui0*CYfigQ-CUBuilder-compY3sJzcore0*d8JD8Manager-p4JmodvGJ*CYfiguratiY-2WkState-2Cli3State-*slotJ*Prohibited6-2Ma_6-2K6-26Mapp_g-*roleJ*zfile0*CommY-ModVG-zobjet0*itemJMod9s-*6Barrier9-3ityJCustomDrop9H%5Cn%27)~versiY!%271%27)*%20%20-H0*0%5Cn*2**3ent4layer5Mix_-6Slot7nv3ory8istributiY9ItemGalidityH.javaJ02KRoleQScreenUommandXAbstractYonZ-2NewP4_inkorldqEv3sz...0%01zqk_ZYXUQKJHG987654320-*

※クラス名はイメージしやすいようにするための例です。

com.github.godhexagon.oneslotsurvival/
├── OneSlotSurvivalMod.java
├── mixin/
│   ├── world/
│   │   ├── inventory/
│   │   │   ├── SlotMixin.java
│   │   │   ├── ItemMixin.java
│   │   │   └── AbstractPushable.java
│   │   └── mob/
│   │       ├── DropItemMixin.java
│   │       └── AbstractMobMixin.java
│   └── client/
│       └── inventory/
│           ├── AbstractContainerScreenMixin.java
│           ├── InventorySlotMixin.java
│           └── TooltipMixin.java
├── event/
│   ├── ForgeWorldEvents.java
│   ├── ForgeClientEvents.java
│   ├── DropItemEvents.java
│   └── ContainerScreenEvents.java
├── inventoryui/ #転職機能とレベルアップ機能の操作をするUIです。Mixinから直接参照されます。
│   └── ...
├── configui/ #サーバー設定GUIをScreenで定義します。
│   ├── ConfigScreen.java
│   ├── CommandBuilder.java
│   └── components/
│       └── ...
├── command/
│   ├── player/
│   │   ├── RoleCommand.java
│   │   └── SlotCommand.java
│   └── admin/ #このパッケージより下層は、機能別パッケージです。「管理者ワールド設定コマンド」「管理者そのほか一過性コマンド」
│       ├── world/
│       │   ├── InventoryScreenCompatibility.java
│       │   ├── RoleChanging.java
│       │   ├── NewPlayerValidity.java
│       │   ├── NewPlayerRole.java
│       │   └── NewPlayerItems.java
│       └── action/
│           ├── PlayerValidity.java
│           ├── PlayerRole.java
│           ├── PlayerLevel.java
│           └── RoleDistribution
├── core/
│   ├── distribution/ #プレイヤーのロールが全体的に均一になるようにする統合ロジックです。
│   │   └── DistributionManager.java
│   └── player/
│       ├── modvalidity/
│       │   ├── Configuration.java
│       │   ├── WorldState.java
│       │   └── ClientState.java
│       ├── slot/
│       │   ├── ProhibitedSlot.java
│       │   ├── MainSlot.java
│       │   ├── RoleSlot.java
│       │   └── SlotMapping.java
│       └── role/
│           └── ...
├── file/ #ワールドデータの永続化
│   ├── Common.java
│   ├── ModValidity.java
│   └── ...
└── object/
├── item/
│   ├── ModItems.java
│   └── SlotBarrierItem.java
└── entity/
└── CustomDropItem.java