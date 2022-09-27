package me.char321.chunkcacher;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelInfo;

import java.util.HashMap;
import java.util.Map;

public class WorldCache {
    public static boolean isGenerating = false;
    public static LevelInfo lastGeneratorOptions = null;
    public static Map<DimensionType, Long2ObjectLinkedOpenHashMap<CompoundTag>> cache = new HashMap<>();

    public static void addChunk(ChunkPos chunkPos, Chunk chunk, ServerWorld world) {
        cache.computeIfAbsent(world.getDimension().getType(), k -> new Long2ObjectLinkedOpenHashMap<>()).put(chunkPos.toLong(), ChunkSerializer.serialize(world, chunk));
    }

    public static boolean shouldCache() {
        return isGenerating;
    }

    public static CompoundTag getChunkNbt(ChunkPos chunkPos, ServerWorld world) {
        Long2ObjectLinkedOpenHashMap<CompoundTag> map = cache.get(world.getDimension().getType());
        if (map == null) return null;
        return map.get(chunkPos.toLong());
    }

    /**
     * Checks if the generator options have changed, if so, clear the cache
     * dude github copilot is so cool it auto generated these comments
     */
    public static void checkGeneratorOptions(LevelInfo generatorOptions) {
        if (lastGeneratorOptions == null ||
                lastGeneratorOptions.getSeed() != generatorOptions.getSeed() ||
                lastGeneratorOptions.hasStructures() != generatorOptions.hasStructures() ||
                lastGeneratorOptions.hasBonusChest() != generatorOptions.hasBonusChest() ||
//TODO: different superflat presets for example are not detected, so the cache is not cleared and the world is not generated correctly
                lastGeneratorOptions.getGeneratorType() != generatorOptions.getGeneratorType()) {
            cache.clear();
            lastGeneratorOptions = generatorOptions;
        }
    }
}
