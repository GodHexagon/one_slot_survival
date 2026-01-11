package com.github.godhexagon.oneslotsurvival.gametest;

import com.github.godhexagon.oneslotsurvival.OneSlotSurvivalMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Consumer;

/**
 * Registers game tests for this mod using Forge's DeferredRegister system.
 * This integrates with Minecraft's TEST_FUNCTION registry.
 */
public class ModGameTestLoader {

    // DeferredRegister for TEST_FUNCTION registry
    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTIONS =
            DeferredRegister.create(Registries.TEST_FUNCTION, OneSlotSurvivalMod.MODID);

    // Register test functions
    public static final RegistryObject<Consumer<GameTestHelper>> ALWAYS_PASS =
            TEST_FUNCTIONS.register("always_pass", () -> BasicGameTests.ALWAYS_PASS);

    public static final RegistryObject<Consumer<GameTestHelper>> VERIFY_AIR_BLOCK =
            TEST_FUNCTIONS.register("verify_air_block", () -> BasicGameTests.VERIFY_AIR_BLOCK);

    public static final RegistryObject<Consumer<GameTestHelper>> PLACE_STONE_BLOCK =
            TEST_FUNCTIONS.register("place_stone_block", () -> BasicGameTests.PLACE_STONE_BLOCK);
}
