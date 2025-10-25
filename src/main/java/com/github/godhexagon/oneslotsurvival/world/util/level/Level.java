package com.github.godhexagon.oneslotsurvival.world.util.level;

import com.github.godhexagon.oneslotsurvival.world.util.attribute.MainRoleLevel;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class Level {
    /**
     * プレイヤーのメインロールのレベルを取得する。
     * 
     * @param player
     * @return レベル。
     */
    public static int getMain(Player player) {
        return Math.max(1, MainRoleLevel.getLevel(player));
    }

    /**
     * プレイヤーのレベルと経験値の両方を初期化する。
     * 
     * @param player
     */
    public static void reset(ServerPlayer player) {
        MainRoleLevel.setLevel(player, 0);
    }
}
