package com.github.godhexagon.oneslotsurvival.test;

import net.minecraft.world.level.GameType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Test class to explore GameType usage in Minecraft 1.21
 * This class is for research purposes only - to understand how GameType is used in vanilla code
 */
public class GameTypeUsageTest {

    /**
     * Explore GameType enum values
     */
    public void exploreGameTypeValues() {
        // Get all GameType values
        GameType survival = GameType.SURVIVAL;
        GameType creative = GameType.CREATIVE;
        GameType adventure = GameType.ADVENTURE;
        GameType spectator = GameType.SPECTATOR;

        // Explore GameType methods (IDE will show available methods)
        // Common methods to check:
        // - getId()
        // - getName()
        // - getShortName()
        // - byId()
        // - byName()
    }

    /**
     * Explore how GameType is used with ServerPlayer
     */
    public void exploreServerPlayerGameType() {
        exploreServerPlayerGameType(null);
    }

    /**
     * Explore how GameType is used with ServerPlayer
     */
    public void exploreServerPlayerGameType(ServerPlayer player) {
        // Check how to get game mode from player
        // player.gameMode (likely a ServerPlayerGameMode object)

        // Check how to set game mode
        // player.setGameMode(GameType)

        // Check related methods
        // player.isCreative()
        // player.isSpectator()
    }

    /**
     * Explore how GameType affects player abilities
     */
    public void explorePlayerAbilities(Player player) {
        // Check how GameType affects:
        // - player.getAbilities() (returns Abilities object)
        // - Can break blocks?
        // - Can fly?
        // - Can interact with entities?
        // - Can take damage?
        // - Can pick up items?
    }
}
