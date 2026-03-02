package com.github.godhexagon.oneslotsurvival.object.storage;

/**
 * プレイヤーに配布されるボーナスアイテムの種類を表す列挙型。
 *
 * <p>初回ログイン時や、リスポーン時に配布されるアイテムを定義します。</p>
 */
public enum BonusItem {
    /**
     * ボーナスアイテムなし（初回ログイン時のみ）
     */
    NONE("none", "None", false),

    /**
     * バンドル（初回ログイン時のみ）
     */
    BUNDLE("bundle", "Bundle", false),

    /**
     * シュルカーボックス（初回ログイン時のみ）
     */
    SHULKERBOX("shulkerbox", "Shulker Box", false),

    /**
     * バンドル（初回ログイン + リスポーン時）
     */
    BUNDLE_RESPAWN("bundle_respawn", "Bundle (+ Respawn)", true),

    /**
     * シュルカーボックス（初回ログイン + リスポーン時）
     */
    SHULKERBOX_RESPAWN("shulkerbox_respawn", "Shulker Box (+ Respawn)", true);

    /** コマンドで使用する名前 */
    private final String commandName;

    /** 表示名 */
    private final String displayName;

    /** リスポーン時にも配布するか */
    private final boolean giveOnRespawn;

    BonusItem(String commandName, String displayName, boolean giveOnRespawn) {
        this.commandName = commandName;
        this.displayName = displayName;
        this.giveOnRespawn = giveOnRespawn;
    }

    /**
     * コマンド名を取得します。
     *
     * @return コマンド名
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * 表示名を取得します。
     *
     * @return 表示名
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * リスポーン時にも配布するかを取得します。
     *
     * @return リスポーン時にも配布する場合true
     */
    public boolean shouldGiveOnRespawn() {
        return giveOnRespawn;
    }

    /**
     * コマンド名からBonusItemを取得します。
     *
     * @param commandName コマンド名
     * @return 対応するBonusItem、見つからない場合はNONE
     */
    public static BonusItem fromCommandName(String commandName) {
        for (BonusItem item : values()) {
            if (item.commandName.equalsIgnoreCase(commandName)) {
                return item;
            }
        }
        return NONE;
    }
}
