# Attribute実装ガイド

**いつ読むか**: プレイヤーデータをAttributeシステムで管理したいとき

## Attributeシステムの概要

- **用途**: エンティティ（プレイヤー含む）の数値プロパティを管理
- **メリット**: 自動永続化、自動クライアント同期、シンプルな実装
- **デメリット**: boolean値は0.0/1.0として扱う必要がある

## 実装パターン

### 1. カスタムAttributeの定義

```java
public class ModAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, MODID);

    public static final RegistryObject<Attribute> MY_ATTRIBUTE = ATTRIBUTES.register(
        "my_attribute",
        () -> new RangedAttribute(
            "attribute.modid.my_attribute",
            0.0,  // default value
            0.0,  // min value
            1.0   // max value
        ).setSyncable(true)  // 重要: クライアント同期を有効化
    );
}
```

### 2. MODクラスでの登録

```java
public ModName(FMLJavaModLoadingContext context) {
    var modBusGroup = context.getModBusGroup();

    // Attributeを登録
    ModAttributes.ATTRIBUTES.register(modBusGroup);

    // イベントハンドラーを登録
    net.minecraftforge.event.entity.EntityAttributeModificationEvent.getBus(modBusGroup)
        .addListener(MyAttributeHandler::onEntityAttributeModification);
}
```

### 3. プレイヤーへのAttribute付与

```java
public class MyAttributeHandler {
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        event.add(
            EntityType.PLAYER,
            ModAttributes.MY_ATTRIBUTE.getHolder().orElseThrow(
                () -> new IllegalStateException(
                    "MY_ATTRIBUTE is not registered. " +
                    "This indicates an issue with DeferredRegister initialization."
                )
            )
        );
    }
}
```

### 4. Attributeの取得・設定

```java
public class MyDataManager {

    // 安全なHolder取得
    private static Holder<Attribute> getAttributeHolder() {
        return ModAttributes.MY_ATTRIBUTE.getHolder().orElseThrow(
            () -> new IllegalStateException(
                "MY_ATTRIBUTE is not registered. " +
                "This indicates a mod initialization error."
            )
        );
    }

    // 値の取得
    public static boolean isEnabled(Player player) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            return false;  // デフォルト値
        }
        return attribute.getBaseValue() > 0.5;  // boolean判定
    }

    // 値の設定
    public static void setEnabled(Player player, boolean enabled) {
        AttributeInstance attribute = player.getAttribute(getAttributeHolder());
        if (attribute == null) {
            throw new IllegalStateException(
                "Player does not have MY_ATTRIBUTE. " +
                "Check EntityAttributeModificationEvent handling."
            );
        }
        attribute.setBaseValue(enabled ? 1.0 : 0.0);
        // 永続化とクライアント同期は自動
    }
}
```

## 重要なポイント

### Optional処理

`RegistryObject#getHolder()`はOptionalを返すため、必ず`orElseThrow()`で安全に処理：

```java
// 良い例
ModAttributes.MY_ATTRIBUTE.getHolder().orElseThrow(
    () -> new IllegalStateException("明確なエラーメッセージ")
)

// 悪い例
ModAttributes.MY_ATTRIBUTE.getHolder().get()  // NoSuchElementException
```

### Boolean値の扱い

Attributeはdouble型なので、booleanは以下のように扱う：
- `0.0` = false
- `1.0` = true
- 判定: `value > 0.5` （浮動小数点誤差対策）

### クライアント同期

`.setSyncable(true)` を**必ず**設定すること。これにより：
- サーバー側の変更が自動的にクライアントに同期
- カスタムパケット実装不要

### 永続化

- Attributeの基底値（BaseValue）は自動的にNBTに保存
- `setBaseValue()` で変更した値はログアウト/ログインで保持
- ワールドごとに独立して保存

## トラブルシューティング

### 「Player does not have MY_ATTRIBUTE」エラー

原因: `EntityAttributeModificationEvent` が正しく処理されていない

解決策:
1. イベントハンドラーがMODバスに登録されているか確認
2. `event.add(EntityType.PLAYER, ...)` が呼ばれているか確認

### 値が保存されない

原因: `setBaseValue()` ではなく `addTransientModifier()` を使用している

解決策: 永続化するには必ず `setBaseValue()` を使用

### クライアントで値が取得できない

原因: `.setSyncable(true)` を設定していない

解決策: RangedAttribute作成時に必ず `.setSyncable(true)` を呼ぶ

## 参考実装

本プロジェクトの実装:
- `ModAttributes.java` - Attribute定義
- `PlayerAttributeHandler.java` - プレイヤーへの付与
- `PlayerModValidity.java` - 値の取得・設定
