package com.github.godhexagon.oneslotsurvival.world.util.role;

import net.minecraft.network.chat.Component;

/**
 * プレイヤーのメインロール定義
 * <p>
 * 各ロールはユニークなIDを持ち、プレイヤーのAttributeとして永続化される。
 * </p>
 */
public enum MainRole {
    /**
     * 不明な状態
     */
    ERROR(-1, "error"),

    /**
     * ロール未割り当て状態
     */
    UNASSIGNED(0, "unassigned"),

    /**
     * 採掘特化ロール
     */
    MINER(1, "miner"),

    /**
     * 戦闘特化ロール
     */
    WARRIOR(2, "warrior");

    private final int atttributeId;
    private final String commandName;

    MainRole(int attribute_id, String command_name) {
        this.atttributeId = attribute_id;
        this.commandName = command_name;
    }

    /**
     * ロールIDを取得
     *
     * @return ロールID（UNASSIGNEDの場合は-1）
     */
    public int getAtttributeId() {
        return atttributeId;
    }

    /**
     * ロール名を取得
     *
     * @return ロールの内部名
     */
    public String getCommandName() {
        return commandName;
    }

    /**
     * 翻訳可能な表示名を取得
     *
     * @return 翻訳キーを含むComponent
     */
    public Component getDisplayName() {
        return Component.translatable("role.oneslotsurvival." + commandName);
    }

    /**
     * IDからロールを取得
     *
     * @param id ロールID
     * @return 対応するMainRole
     */
    public static MainRole fromAttributeId(int id) {
        for (MainRole role : values()) {
            if (role.atttributeId == id) {
                return role;
            }
        }
        return ERROR;
    }

    /**
     * 名前からロールを取得（大文字小文字を区別しない）
     *
     * @param name ロール名
     * @return 対応するMainRole
     */
    public static MainRole fromCommandName(String name) {
        for (MainRole role : values()) {
            if (role.commandName.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return ERROR;
    }
}
