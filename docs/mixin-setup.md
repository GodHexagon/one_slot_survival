## Mixin環境構築

**いつ読むか**: プロジェクトに初めてMixinを導入するとき、またはMixin設定でトラブルが起きたとき

### build.gradle設定

```gradle
plugins {
    // 既存のプラグイン...
    id 'org.spongepowered.mixin' version '0.7.+'
}

dependencies {
    // Mixinアノテーションプロセッサ（必須）
    annotationProcessor 'org.spongepowered:mixin:0.8.5:processor'
}

// Mixin設定ブロック
mixin {
    add sourceSets.main, "modid.refmap.json"
    config "modid.mixins.json"
}

minecraft {
    runs {
        configureEach {
            arg "-mixin.config=${mod_id}.mixins.json"
        }
    }
}

tasks.named('jar', Jar) {
    manifest {
        attributes['MixinConfigs'] = "${mod_id}.mixins.json"
    }
}
```

### Mixin設定ファイル作成

```json
// src/main/resources/modid.mixins.json
{
  "required": true,
  "package": "com.yourmod.mixin",
  "compatibilityLevel": "JAVA_21",
  "refmap": "modid.refmap.json",
  "minVersion": "0.8",
  "client": [
    "YourMixinClass"
  ],
  "injectors": {
    "defaultRequire": 1
  }
}
```

### コンパイル確認

- SpongePowered MIXIN Annotation Processor動作確認
- refmap.json生成成功
- searge mappings適用成功

**実装前の動作確認**
```java
// Mixinアノテーションプロセッサの動作ログ
// ノート: SpongePowered MIXIN Annotation Processor Version=0.8.5
// ノート: Writing refmap to [...]/compileJava-refmap.json
// → 正常に動作している証拠
```