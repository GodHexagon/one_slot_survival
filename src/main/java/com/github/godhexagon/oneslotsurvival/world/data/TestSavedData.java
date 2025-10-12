package com.github.godhexagon.oneslotsurvival.world.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

/**
 * Test class to explore SavedData API through "compile-driven development"
 * This class helps us understand the method signatures and usage patterns
 *
 * DISCOVERY: Modern SavedData API uses Codec-based serialization instead of NBT!
 */
public class TestSavedData extends SavedData {

    private int exampleCounter;
    private String exampleData;

    // Constructor for new instances (required by codec)
    public TestSavedData() {
        this(0, "");
    }

    // Constructor with fields (used by codec)
    public TestSavedData(int counter, String data) {
        this.exampleCounter = counter;
        this.exampleData = data;
    }

    // Business logic methods
    public void incrementCounter() {
        this.exampleCounter++;
        this.setDirty(); // Must call to trigger save
    }

    public int getCounter() {
        return this.exampleCounter;
    }

    public void setExampleData(String data) {
        this.exampleData = data;
        this.setDirty(); // Must call to trigger save
    }

    public String getExampleData() {
        return this.exampleData;
    }

    // Static helper methods to get/create SavedData instances

    /**
     * SavedDataType definition - Forge 1.21.8 API uses Codec for serialization
     * Constructor signature: SavedDataType(String id, Supplier<T> constructor, Codec<T> codec, DataFixTypes dataFixType)
     *
     * For custom mod SavedData, use null for dataFixType (vanilla data uses specific types)
     */
    public static final SavedDataType<TestSavedData> TYPE = new SavedDataType<>(
        "test_saved_data",  // ID for the .dat file
        TestSavedData::new,  // Constructor supplier for new instances
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("counter").forGetter((TestSavedData sd) -> sd.exampleCounter),
            Codec.STRING.fieldOf("data").forGetter((TestSavedData sd) -> sd.exampleData)
        ).apply(instance, TestSavedData::new)),  // Codec for serialization/deserialization
        null  // DataFixTypes - null for custom mod data
    );

    /**
     * Get or create SavedData for a specific level (dimension)
     * Modern API uses SavedDataType as single parameter
     */
    public static TestSavedData getForLevel(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }

    /**
     * Get or create global SavedData (attached to Overworld)
     */
    public static TestSavedData getGlobal(MinecraftServer server) {
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            throw new IllegalStateException("Overworld not loaded");
        }
        return getForLevel(overworld);
    }
}
