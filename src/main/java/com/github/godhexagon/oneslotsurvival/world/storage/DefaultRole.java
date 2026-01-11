package com.github.godhexagon.oneslotsurvival.world.storage;

/**
 * プレイヤーの初期ロール割り当て方式を表す列挙型。
 *
 * <p>ワールドに初めてログインしたプレイヤーに対して、どのようにロールを割り当てるかを定義します。</p>
 */
public enum DefaultRole {
    /**
     * ロールを割り当てない（未割り当て状態）
     */
    UNASSIGN("unassign", "Unassign"),

    /**
     * ランダムにロールを割り当てる
     */
    RANDOM("random", "Random"),

    /**
     * 定義されたリストから順番に割り当てる
     */
    DEFINED_LIST("definedList", "Defined List");

    /** コマンドで使用する名前 */
    private final String commandName;

    /** 表示名 */
    private final String displayName;

    DefaultRole(String commandName, String displayName) {
        this.commandName = commandName;
        this.displayName = displayName;
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
     * コマンド名からDefaultRoleを取得します。
     *
     * @param commandName コマンド名
     * @return 対応するDefaultRole、見つからない場合はUNASSIGN
     */
    public static DefaultRole fromCommandName(String commandName) {
        for (DefaultRole role : values()) {
            if (role.commandName.equalsIgnoreCase(commandName)) {
                return role;
            }
        }
        return UNASSIGN;
    }
}
