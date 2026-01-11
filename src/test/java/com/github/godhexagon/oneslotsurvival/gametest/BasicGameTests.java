package com.github.godhexagon.oneslotsurvival.gametest;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTest;
import net.minecraftforge.gametest.GameTestNamespace;

/**
 * Basic game tests to verify the GameTest framework is working correctly.
 */
@GameTestNamespace("oneslotsurvival")
public class BasicGameTests {

    /**
     * Simplest possible test - always succeeds immediately.
     * This verifies that the test framework can discover and run tests.
     */
    @GameTest(name = "always_pass")
    public static void alwaysPass(GameTestHelper helper) {
        helper.succeed();
    }

    /**
     * Test that verifies we can check for air blocks.
     * The default structure is "forge:empty3x3x3" which is filled with air.
     */
    @GameTest(name = "verify_air_block")
    public static void verifyAirBlock(GameTestHelper helper) {
        // The default structure is 3x3x3 of air
        helper.assertBlockPresent(Blocks.AIR, 1, 1, 1);
        helper.succeed();
    }

    /**
     * Test that places a block and verifies it exists.
     */
    @GameTest(name = "place_stone_block")
    public static void placeStoneBlock(GameTestHelper helper) {
        helper.setBlock(1, 1, 1, Blocks.STONE);
        helper.assertBlockPresent(Blocks.STONE, 1, 1, 1);
        helper.succeed();
    }
}
