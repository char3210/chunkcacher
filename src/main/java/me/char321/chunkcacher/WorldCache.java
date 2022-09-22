package me.char321.chunkcacher;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GeneratorOptions;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class WorldCache {
    public static boolean isGenerating = false;
    public static GeneratorOptions lastGeneratorOptions = null;
    public static Map<RegistryKey<World>, Long2ObjectLinkedOpenHashMap<CompoundTag>> cache = new HashMap<>();

    public static void addChunk(ChunkPos chunkPos, Chunk chunk, ServerWorld world) {
        cache.computeIfAbsent(world.getRegistryKey(), k -> new Long2ObjectLinkedOpenHashMap<>()).put(chunkPos.toLong(), ChunkSerializer.serialize(world, chunk));
    }

    public static boolean shouldCache() {
        return isGenerating;
    }

    public static CompoundTag getChunkNbt(ChunkPos chunkPos, ServerWorld world) {
        Long2ObjectLinkedOpenHashMap<CompoundTag> map = cache.get(world.getRegistryKey());
        if (map == null) return null;
        return map.get(chunkPos.toLong());
    }

    /**
     * Checks if the generator options have changed, if so, clear the cache
     * dude github copilot is so cool it auto generated these comments
     */
    public static void checkGeneratorOptions(GeneratorOptions generatorOptions) {
        if (lastGeneratorOptions == null) {
            cache.clear();
            lastGeneratorOptions = generatorOptions;
            return;
        }
        //what is an or statement
        if (lastGeneratorOptions.getSeed() != generatorOptions.getSeed()) {
            cache.clear();
            lastGeneratorOptions = generatorOptions;
            return;
        }
        if (lastGeneratorOptions.shouldGenerateStructures() != generatorOptions.shouldGenerateStructures()) {
            cache.clear();
            lastGeneratorOptions = generatorOptions;
            return;
        }
        if (lastGeneratorOptions.hasBonusChest() != generatorOptions.hasBonusChest()) {
            cache.clear();
            lastGeneratorOptions = generatorOptions;
            return;
        }
        //TODO: different superflat presets for example are not detected, so the cache is not cleared and the world is not generated correctly
        if (!lastGeneratorOptions.getChunkGenerator().getClass().equals(generatorOptions.getChunkGenerator().getClass())) {
            cache.clear();
            lastGeneratorOptions = generatorOptions;
        }
    }
}
