package com.github.godhexagon.oneslotsurvival.rule.level;

import com.github.godhexagon.oneslotsurvival.rule.attribute.LevelAttribute;

import net.minecraft.world.entity.player.Player;

/**
 * 永続化レイヤーを隠し、レベルのルールを提供する。
 */
public class Level {
    public static final int UNDEFINED_LEVEL = 0;
    
    /**
     * プレイヤーのメインロールのレベルを取得する。
     * 
     * @param player プレイヤー。
     * @return レベル。
     */
    public static int get(Player player) {
        return Math.max(1, LevelAttribute.getLevel(player));
    }
}
