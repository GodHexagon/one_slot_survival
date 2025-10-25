package com.github.godhexagon.oneslotsurvival.world.util.level;

import com.github.godhexagon.oneslotsurvival.world.util.attribute.MainRoleLevel;

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
    public static int getMain(Player player) {
        return Math.max(1, MainRoleLevel.getLevel(player));
    }

    /**
     * プレイヤーのレベルと経験値の両方を初期化する。
     * 
     * @param player プレイヤー。
     */
    public static void reset(ServerPlayer player) {
        MainRoleLevel.setLevel(player, UNDEFINED_LEVEL);
    }
}
