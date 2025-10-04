package com.github.godhexagon.oneslotsurvival.core.player.modvalidity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;

import java.util.EnumSet;
import java.util.Set;

public class WorldState {
    public static boolean isEffective(Player player) {
        return  isEffective(player, player.gameMode());
    }

    public static boolean isEffective(Player player, GameType gameMode) {
        return Configuration.getValidity(player) && isEffectiveGameMode(gameMode);
    }

    public static boolean isEffectiveGameMode(GameType gameMode) {
        Set<GameType> effectiveGameMode = EnumSet.of(GameType.SURVIVAL, GameType.ADVENTURE);
        return effectiveGameMode.contains(gameMode);
    }
}
