package com.github.godhexagon.oneslotsurvival.world.storage;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.CommandStorage;

/**
 * ゲームデータを管理するクラス。
 *
 * <p>MinecraftのCommandStorage（/data modify storage）を使用してゲームデータを永続化します。</p>
 *
 * <h2>設定方法</h2>
 * <pre>{@code
 * // definedList呼び出しカウンタをリセット
 * /data modify storage oneslotsurvival:gamedata definedListCallCount set value 0
 * }</pre>
 *
 * <h2>コードからのアクセス</h2>
 * <pre>{@code
 * // ServerLevelから
 * int callCount = GameData.getDefinedListCallCount(serverLevel);
 * GameData.incrementDefinedListCallCount(serverLevel);
 *
 * // MinecraftServerから
 * int callCount = GameData.getDefinedListCallCount(server);
 * GameData.incrementDefinedListCallCount(server);
 * }</pre>
 *
 * <h2>デフォルト値</h2>
 * <ul>
 *   <li>definedListCallCount: 0（初期値）</li>
 * </ul>
 */
public class GameData {

    /** ストレージのResourceLocation */
    private static final ResourceLocation STORAGE_ID =
        ResourceLocation.fromNamespaceAndPath(OneSlotSurvivalMod.MODID, "gamedata");

    /** definedList呼び出しカウンタのNBTキー */
    private static final String DEFINED_LIST_CALL_COUNT_KEY = "definedListCallCount";

    /** デフォルト値: 呼び出しカウンタは0から開始 */
    private static final int DEFAULT_CALL_COUNT = 0;

    /**
     * definedList呼び出し回数を取得します。
     *
     * @param level サーバーレベル
     * @return definedList呼び出し回数
     */
    public static int getDefinedListCallCount(ServerLevel level) {
        return getDefinedListCallCount(level.getServer());
    }

    /**
     * definedList呼び出し回数を取得します。
     *
     * @param server MinecraftServer
     * @return definedList呼び出し回数
     */
    public static int getDefinedListCallCount(MinecraftServer server) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        return tag.getIntOr(DEFINED_LIST_CALL_COUNT_KEY, DEFAULT_CALL_COUNT);
    }

    /**
     * definedList呼び出し回数をインクリメントします。
     *
     * @param level サーバーレベル
     * @return インクリメント後の呼び出し回数
     */
    public static int incrementDefinedListCallCount(ServerLevel level) {
        return incrementDefinedListCallCount(level.getServer());
    }

    /**
     * definedList呼び出し回数をインクリメントします。
     *
     * @param server MinecraftServer
     * @return インクリメント後の呼び出し回数
     */
    public static int incrementDefinedListCallCount(MinecraftServer server) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        int currentCount = tag.getIntOr(DEFINED_LIST_CALL_COUNT_KEY, DEFAULT_CALL_COUNT);
        int newCount = currentCount + 1;

        tag.putInt(DEFINED_LIST_CALL_COUNT_KEY, newCount);
        storage.set(STORAGE_ID, tag);

        return newCount;
    }

    /**
     * definedList呼び出し回数を設定します。
     *
     * @param server MinecraftServer
     * @param count 設定する呼び出し回数
     */
    public static void setDefinedListCallCount(MinecraftServer server, int count) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        tag.putInt(DEFINED_LIST_CALL_COUNT_KEY, count);
        storage.set(STORAGE_ID, tag);
    }
}
