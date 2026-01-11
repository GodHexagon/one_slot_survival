package com.github.godhexagon.oneslotsurvival.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

/**
 * Basic game tests to verify the GameTest framework is working correctly.
 * Uses vanilla Minecraft GameTest API (net.minecraft.gametest.framework).
 */
public class BasicGameTests {

    /**
     * Simplest possible test - always succeeds immediately.
     * This verifies that the test framework can discover and run tests.
     */
    public static final Consumer<GameTestHelper> ALWAYS_PASS = helper -> {
        helper.succeed();
    };

    /**
     * Test that verifies we can check for air blocks.
     * The default structure is "forge:empty3x3x3" which is filled with air.
     */
    public static final Consumer<GameTestHelper> VERIFY_AIR_BLOCK = helper -> {
        // The default structure is 3x3x3 of air
        helper.assertBlockPresent(Blocks.AIR, 1, 1, 1);
        helper.succeed();
    };

    /**
     * Test that places a block and verifies it exists.
     */
    public static final Consumer<GameTestHelper> PLACE_STONE_BLOCK = helper -> {
        helper.setBlock(1, 1, 1, Blocks.STONE);
        helper.assertBlockPresent(Blocks.STONE, 1, 1, 1);
        helper.succeed();
    };
}
