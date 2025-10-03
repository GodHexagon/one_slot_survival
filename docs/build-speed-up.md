# ビルド速度上昇施策

## runClient利用

現在のプロジェクトでは、すでに差分ビルドが有効になっています。

## Gradle Daemon有効化

`gradle.properties`で`org.gradle.daemon=true`に設定することで、Gradle Daemonが有効化され、ビルドとrunClientの起動が高速化されます。

## JVMメモリ最適化

### Gradleプロセス
`gradle.properties`にて以下を設定：
- `-Xms5G -Xmx5G`: 起動時と最大メモリを同じに設定し、メモリ再割り当てのオーバーヘッドを削減
- G1GCの使用: `-XX:+UseG1GC`及び関連パラメータでガベージコレクションを最適化

### runClientプロセス
`build.gradle`のclient run設定にて以下のJVM引数を追加：
- `-Xms4G -Xmx4G`: Minecraftクライアント用に4GBを割り当て
- G1GCパラメータ: 低遅延のガベージコレクション設定

## ホットスワップ

Intelij IDEAのホットスワップ機能を利用する。「起動構成」を追加して「Debug」ボタンを押してゲームを起動することで利用できる。

## キャッシュ
