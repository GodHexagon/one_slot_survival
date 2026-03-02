package com.github.godhexagon.oneslotsurvival.object.storage;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.CommandStorage;

/**
 * ワールドオプション設定を管理するクラス。
 *
 * <p>MinecraftのCommandStorage（/data modify storage）を使用して設定を永続化します。</p>
 *
 * <h2>設定方法</h2>
 * <pre>{@code
 * // メインロール変更を無効化
 * /data modify storage oneslotsurvival:world_options mainRoleChanging set value 0b
 *
 * // サブロール変更を無効化
 * /data modify storage oneslotsurvival:world_options subRoleChanging set value 0b
 *
 * // 初期MOD有効性を無効化
 * /data modify storage oneslotsurvival:world_options defaultModValidity set value 0b
 *
 * // 初期ロール割り当て方式を設定
 * /data modify storage oneslotsurvival:world_options defaultRole set value "random"
 *
 * // 有効化する場合
 * /data modify storage oneslotsurvival:world_options mainRoleChanging set value 1b
 * /data modify storage oneslotsurvival:world_options subRoleChanging set value 1b
 * /data modify storage oneslotsurvival:world_options defaultModValidity set value 1b
 * }</pre>
 *
 * <h2>コードからのアクセス</h2>
 * <pre>{@code
 * // ServerLevelから
 * boolean canChangeMainRole = WorldOptions.isMainRoleChangingEnabled(serverLevel);
 * boolean canChangeSubRole = WorldOptions.isSubRoleChangingEnabled(serverLevel);
 * boolean defaultModValidity = WorldOptions.isDefaultModValidityEnabled(serverLevel);
 * DefaultRole defaultRole = WorldOptions.getDefaultRole(serverLevel);
 *
 * // MinecraftServerから
 * boolean canChangeMainRole = WorldOptions.isMainRoleChangingEnabled(server);
 * boolean canChangeSubRole = WorldOptions.isSubRoleChangingEnabled(server);
 * boolean defaultModValidity = WorldOptions.isDefaultModValidityEnabled(server);
 * DefaultRole defaultRole = WorldOptions.getDefaultRole(server);
 * }</pre>
 *
 * <h2>デフォルト値</h2>
 * <p>デフォルト値はIntegrated Server（シングルプレイ）とDedicated Server（マルチプレイ）で異なります。</p>
 * <table border="1">
 *   <tr>
 *     <th>設定項目</th>
 *     <th>Integrated</th>
 *     <th>Dedicated</th>
 *   </tr>
 *   <tr>
 *     <td>mainRoleChanging</td>
 *     <td>true（変更可能）</td>
 *     <td>false（変更不可）</td>
 *   </tr>
 *   <tr>
 *     <td>subRoleChanging</td>
 *     <td>true（変更可能）</td>
 *     <td>false（変更不可）</td>
 *   </tr>
 *   <tr>
 *     <td>defaultModValidity</td>
 *     <td>true（有効）</td>
 *     <td>true（有効）</td>
 *   </tr>
 *   <tr>
 *     <td>bonusItem</td>
 *     <td>NONE</td>
 *     <td>NONE</td>
 *   </tr>
 *   <tr>
 *     <td>defaultRole</td>
 *     <td>RANDOM</td>
 *     <td>RANDOM</td>
 *   </tr>
 *   <tr>
 *     <td>keepRoleProgress</td>
 *     <td>true（死亡時に保持）</td>
 *     <td>true（死亡時に保持）</td>
 *   </tr>
 * </table>
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

    /** 初期MOD有効性設定のNBTキー */
    private static final String DEFAULT_MOD_VALIDITY_KEY = "defaultModValidity";

    /** 初期ロール割り当て方式設定のNBTキー */
    private static final String DEFAULT_ROLE_KEY = "defaultRole";

    /** ロール変更時に進捗を保持するかのNBTキー */
    private static final String KEEP_ROLE_PROGRESS_KEY = "keepRoleProgress";

    /**
     * メインロール変更のデフォルト値を取得します。
     *
     * @param server MinecraftServer
     * @return Integratedの場合true、Dedicatedの場合false
     */
    private static boolean getDefaultMainRoleChanging(MinecraftServer server) {
        return !server.isDedicatedServer(); // Integrated: true, Dedicated: false
    }

    /**
     * サブロール変更のデフォルト値を取得します。
     *
     * @param server MinecraftServer
     * @return Integratedの場合true、Dedicatedの場合false
     */
    private static boolean getDefaultSubRoleChanging(MinecraftServer server) {
        return !server.isDedicatedServer(); // Integrated: true, Dedicated: false
    }

    /**
     * 初期MOD有効性のデフォルト値を取得します。
     *
     * @param server MinecraftServer
     * @return 常にtrue
     */
    private static boolean getDefaultModValidity(MinecraftServer server) {
        return true; // 両方とも true
    }

    /**
     * ボーナスアイテムのデフォルト値を取得します。
     *
     * @param server MinecraftServer
     * @return 常にNONE
     */
    private static BonusItem getDefaultBonusItem(MinecraftServer server) {
        return BonusItem.NONE; // 両方とも NONE
    }

    /**
     * 初期ロール割り当て方式のデフォルト値を取得します。
     *
     * @param server MinecraftServer
     * @return 常にRANDOM
     */
    private static DefaultRole getDefaultRoleValue(MinecraftServer server) {
        return server.isDedicatedServer()? DefaultRole.DEFINED_LIST:DefaultRole.RANDOM; // Integrated: Random, Dedicated: Defined List
    }

    /**
     * 死亡時に進捗を保持するかのデフォルト値を取得します。
     *
     * @param server MinecraftServer
     * @return 常にtrue（保持）
     */
    private static boolean getDefaultKeepRoleProgress(MinecraftServer server) {
        return true; // 両方とも true
    }

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
        return getBooleanValue(server, MAIN_ROLE_CHANGING_KEY, getDefaultMainRoleChanging(server));
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
        return getBooleanValue(server, SUB_ROLE_CHANGING_KEY, getDefaultSubRoleChanging(server));
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
        String commandName = tag.getStringOr(BONUS_ITEM_KEY, getDefaultBonusItem(server).getCommandName());

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
     * 初期MOD有効性設定を取得します。
     *
     * @param level サーバーレベル
     * @return 初期MOD有効性がtrueの場合true
     */
    public static boolean isDefaultModValidityEnabled(ServerLevel level) {
        return isDefaultModValidityEnabled(level.getServer());
    }

    /**
     * 初期MOD有効性設定を取得します。
     *
     * @param server MinecraftServer
     * @return 初期MOD有効性がtrueの場合true
     */
    public static boolean isDefaultModValidityEnabled(MinecraftServer server) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        return tag.getBooleanOr(DEFAULT_MOD_VALIDITY_KEY, getDefaultModValidity(server));
    }

    /**
     * 初期MOD有効性設定を変更します。
     *
     * @param server MinecraftServer
     * @param enabled 有効にする場合true
     */
    public static void setDefaultModValidityEnabled(MinecraftServer server, boolean enabled) {
        setBooleanValue(server, DEFAULT_MOD_VALIDITY_KEY, enabled);
    }

    /**
     * 初期ロール割り当て方式設定を取得します。
     *
     * @param level サーバーレベル
     * @return 初期ロール割り当て方式
     */
    public static DefaultRole getDefaultRole(ServerLevel level) {
        return getDefaultRole(level.getServer());
    }

    /**
     * 初期ロール割り当て方式設定を取得します。
     *
     * @param server MinecraftServer
     * @return 初期ロール割り当て方式
     */
    public static DefaultRole getDefaultRole(MinecraftServer server) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        // 文字列として保存されているコマンド名を取得
        String commandName = tag.getStringOr(DEFAULT_ROLE_KEY, getDefaultRoleValue(server).getCommandName());

        // コマンド名からDefaultRoleに変換
        return DefaultRole.fromCommandName(commandName);
    }

    /**
     * 初期ロール割り当て方式設定を変更します。
     *
     * @param server MinecraftServer
     * @param defaultRole 設定する初期ロール割り当て方式
     */
    public static void setDefaultRole(MinecraftServer server, DefaultRole defaultRole) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        // コマンド名を文字列として保存
        tag.putString(DEFAULT_ROLE_KEY, defaultRole.getCommandName());

        // 保存
        storage.set(STORAGE_ID, tag);
    }

    /**
     * 死亡時に進捗を保持するかを取得します。
     *
     * @param level サーバーレベル
     * @return 進捗を保持する場合true、リセットする場合false
     */
    public static boolean isKeepRoleProgressEnabled(ServerLevel level) {
        return isKeepRoleProgressEnabled(level.getServer());
    }

    /**
     * 死亡時に進捗を保持するかを取得します。
     *
     * @param server MinecraftServer
     * @return 進捗を保持する場合true、リセットする場合false
     */
    public static boolean isKeepRoleProgressEnabled(MinecraftServer server) {
        return getBooleanValue(server, KEEP_ROLE_PROGRESS_KEY, getDefaultKeepRoleProgress(server));
    }

    /**
     * 死亡時に進捗を保持するかを設定します。
     *
     * @param server MinecraftServer
     * @param enabled 保持する場合true、リセットする場合false
     */
    public static void setKeepRoleProgressEnabled(MinecraftServer server, boolean enabled) {
        setBooleanValue(server, KEEP_ROLE_PROGRESS_KEY, enabled);
    }

    /**
     * CommandStorageから指定されたキーのboolean値を取得します。
     *
     * @param server MinecraftServer
     * @param key NBTキー
     * @param defaultValue デフォルト値
     * @return 設定値（見つからない場合はデフォルト値）
     */
    private static boolean getBooleanValue(MinecraftServer server, String key, boolean defaultValue) {
        CommandStorage storage = server.getCommandStorage();
        CompoundTag tag = storage.get(STORAGE_ID);

        // byte値として保存されている（1b = true, 0b = false）
        // getBooleanOr()を使用してデフォルト値を指定
        return tag.getBooleanOr(key, defaultValue);
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
