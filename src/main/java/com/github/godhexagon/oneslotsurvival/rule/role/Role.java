package com.github.godhexagon.oneslotsurvival.rule.role;

import net.minecraft.network.chat.Component;

/**
 * メインロールとサブロールの共通インターフェース
 */
public interface Role {
    
    /**
     * ロールIDを取得
     *
     * @return ロールID
     */
    int getAttributeId();

    /**
     * コマンド名を取得
     *
     * @return コマンド名
     */
    String getCommandName();

    /**
     * 翻訳可能な表示名を取得
     *
     * @return 翻訳キーを含むComponent
     */
    Component getDisplayName();
}
