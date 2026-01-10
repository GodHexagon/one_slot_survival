package com.github.godhexagon.oneslotsurvival.world.storage;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.CommandStorage;

/**
 * ロール変更設定を管理するクラス。
 *
 * <p>MinecraftのCommandStorage（/data modify storage）を使用して設定を永続化します。</p>
 *
 * <h2>設定方法</h2>
 * <pre>{@code
 * // メインロール変更を無効化
 * /data modify storage oneslotsurvival:settings mainRoleChanging set value 0b
 *
 * // サブロール変更を無効化
 * /data modify storage oneslotsurvival:settings subRoleChanging set value 0b
 *
 * // 有効化する場合
 * /data modify storage oneslotsurvival:settings mainRoleChanging set value 1b
 * /data modify storage oneslotsurvival:settings subRoleChanging set value 1b
 * }</pre>
 *
 * <h2>コードからのアクセス</h2>
 * <pre>{@code
 * // ServerLevelから
 * boolean canChangeMainRole = RoleChangingSettings.isMainRoleChangingEnabled(serverLevel);
 * boolean canChangeSubRole = RoleChangingSettings.isSubRoleChangingEnabled(serverLevel);
 *
 * // MinecraftServerから
 * boolean canChangeMainRole = RoleChangingSettings.isMainRoleChangingEnabled(server);
 * boolean canChangeSubRole = RoleChangingSettings.isSubRoleChangingEnabled(server);
 * }</pre>
 *
 * <h2>デフォルト値</h2>
 * <ul>
 *   <li>mainRoleChanging: true（変更可能）</li>
 *   <li>subRoleChanging: true（変更可能）</li>
 * </ul>
 */
public class WorldOptions {

    /** ストレージのResourceLocation */
    private static final ResourceLocation STORAGE_ID =
        ResourceLocation.fromNamespaceAndPath(OneSlotSurvivalMod.MODID, "world_options");

    /** メインロール変更設定のNBTキー */
    private static final String MAIN_ROLE_CHANGING_KEY = "mainRoleChanging";

    /** サブロール変更設定のNBTキー */
    private static final String SUB_ROLE_CHANGING_KEY = "subRoleChanging";

    /** ボーナスアイテム設定のNBTキー */
    private static final String BONUS_ITEM_KEY = "bonusItem";

    /** デフォルト値: 変更可能 */
    private static final boolean DEFAULT_VALUE = true;

    /** デフォルト値: ボーナスアイテムなし */
    private static final BonusItem DEFAULT_BONUS_ITEM = BonusItem.NONE;

    /**
     * メインロール変更が有効かどうかを取得します。
     *
     * @param level サーバーレベル
     * @return メインロール変更が有効な場合true
     */
    public static boolean isMainRoleChangingEnabled(ServerLevel level) {
        return isMainRoleChangingEnabled(level.getServer());
    }

    /**
     * メインロール変更が有効かどうかを取得します。
     *
     * @param server MinecraftServer
     * @return メインロール変更が有効な場合true
     */
    public static boolean isMainRoleChangingEnabled(MinecraftServer server) {
        return getBooleanValue(server, MAIN_ROLE_CHANGING_KEY);
    }

    /**
     * サブロール変更が有効かどうかを取得します。
     *
     * @param level サーバーレベル
     * @return サブロール変更が有効な場合true
     */
    public static boolean isSubRoleChangingEnabled(ServerLevel level) {
        return isSubRoleChangingEnabled(level.getServer());
    }

    /**
     * サブロール変更が有効かどうかを取得します。
     *
     * @param server MinecraftServer
     * @return サブロール変更が有効な場合true
     */
    public static boolean isSubRoleChangingEnabled(MinecraftServer server) {
        return getBooleanValue(server, SUB_ROLE_CHANGING_KEY);
    }

    /**
     * メインロール変更の有効/無効を設定します。
     *
     * @param server MinecraftServer
     * @param enabled 有効にする場合true
     */
    public static void setMainRoleChangingEnabled(MinecraftServer server, boolean enabled) {
        setBooleanValue(server, MAIN_ROLE_CHANGING_KEY, enabled);
    }

    /**
     * サブロール変更の有効/無効を設定します。
     *
     * @param server MinecraftServer
     * @param enabled 有効にする場合true
     */
    public static void setSubRoleChangingEnabled(MinecraftServer server, boolean enabled) {
        setBooleanValue(server, SUB_ROLE_CHANGING_KEY, enabled);
    }

    /**
     * ボーナスアイテム設定を取得します。
     *
     * @param level サーバーレベル
     * @return ボーナスアイテム設定
     */
    public static BonusItem getBonusItem(ServerLevel level) {
        return getBonusItem(level.getServer());
    }

    /**
     * ボーナスアイテム設定を取得します。
     *
     * @param server MinecraftServer
     * @return ボーナスアイテム設定
     */
    public static BonusItem getBonusItem(MinecraftServer server) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        // 文字列として保存されているコマンド名を取得
        // getStringOrを使用してデフォルト値を指定
        String commandName = tag.getStringOr(BONUS_ITEM_KEY, DEFAULT_BONUS_ITEM.getCommandName());

        // コマンド名からBonusItemに変換
        return BonusItem.fromCommandName(commandName);
    }

    /**
     * ボーナスアイテム設定を変更します。
     *
     * @param server MinecraftServer
     * @param bonusItem 設定するボーナスアイテム
     */
    public static void setBonusItem(MinecraftServer server, BonusItem bonusItem) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        // コマンド名を文字列として保存
        tag.putString(BONUS_ITEM_KEY, bonusItem.getCommandName());

        // 保存
        storage.set(STORAGE_ID, tag);
    }

    /**
     * CommandStorageから指定されたキーのboolean値を取得します。
     *
     * @param server MinecraftServer
     * @param key NBTキー
     * @return 設定値（見つからない場合はデフォルト値）
     */
    private static boolean getBooleanValue(MinecraftServer server, String key) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        // byte値として保存されている（1b = true, 0b = false）
        // getBooleanOr()を使用してデフォルト値を指定
        return tag.getBooleanOr(key, DEFAULT_VALUE);
    }

    /**
     * CommandStorageに指定されたキーのboolean値を設定します。
     *
     * @param server MinecraftServer
     * @param key NBTキー
     * @param value 設定値
     */
    private static void setBooleanValue(MinecraftServer server, String key, boolean value) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        // 値を設定
        tag.putBoolean(key, value);

        // 保存
        storage.set(STORAGE_ID, tag);
    }
}
