package com.github.godhexagon.oneslotsurvival.rule.level;

import com.github.godhexagon.oneslotsurvival.rule.attribute.LevelAttribute;

import net.minecraft.server.level.ServerPlayer;
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

    /**
     * プレイヤーのレベルと経験値の両方を初期化する。
     * 
     * @param player プレイヤー。
     */
    public static void reset(ServerPlayer player) {
        LevelAttribute.setLevel(player, UNDEFINED_LEVEL);
        RoleLeveledUpTimes.resetMain(player);
    }
}
