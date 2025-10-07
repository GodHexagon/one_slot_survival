# worldとclientに関する調査

謎：ワールドの変更なのに、クライアントを除くってどういうこと？

```java
@net.minecraftforge.eventbus.api.listener.SubscribeEvent
public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
    // Only process on server side
    if (event.player.level().isClientSide) {
        return;
    }
    ...
}
```

推測：サーバーとクライアントは両方「ワールド」を保持していて、Minecraftの機能により同期している。ワールドのロジックのうち、クライアントでの発火を除くことができる。

```java
// onPlayerTickイベントは、ワールドロジックへの挿入。サーバーとクライアントで１回ずつ実行されている。
```

調査方法：サーバーで実行、クライアントで実行、制限なしをそれぞれテストし、見比べる。

```java
@net.minecraftforge.eventbus.api.listener.SubscribeEvent
public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
    // Client side
    if (event.player.level().isClientSide) {
        execute();
    }

    // Server only
    if (!event.player.level().isClientSide) {
        execute();
    }
    
    // Both
    execute();
}
```
