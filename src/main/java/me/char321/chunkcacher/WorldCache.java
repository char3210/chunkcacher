package me.char321.chunkcacher;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GeneratorOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorldCache {
    public static boolean isGenerating = false;
    public static GeneratorOptions lastGeneratorOptions = null;
    public static Map<RegistryKey<World>, Long2ObjectLinkedOpenHashMap<NbtCompound>> cache = new HashMap<>();
    public static List<ChunkPos> strongholdCache;

    public static void addChunk(ChunkPos chunkPos, Chunk chunk, ServerWorld world) {
        cache.computeIfAbsent(world.getRegistryKey(), k -> new Long2ObjectLinkedOpenHashMap<>()).put(chunkPos.toLong(), ChunkSerializer.serialize(world, chunk));
    }

    public static boolean shouldCache() {
        return isGenerating;
    }

    public static NbtCompound getChunkNbt(ChunkPos chunkPos, ServerWorld world) {
        Long2ObjectLinkedOpenHashMap<NbtCompound> map = cache.get(world.getRegistryKey());
        if (map == null) return null;
        return map.get(chunkPos.toLong());
    }

    /**
     * Checks if the generator options have changed, if so, clear the cache
     * dude github copilot is so cool it auto generated these comments
     */
    public static void checkGeneratorOptions(GeneratorOptions generatorOptions) {
        if (lastGeneratorOptions == null ||
                lastGeneratorOptions.getSeed() != generatorOptions.getSeed() ||
                lastGeneratorOptions.shouldGenerateStructures() != generatorOptions.shouldGenerateStructures() ||
                lastGeneratorOptions.hasBonusChest() != generatorOptions.hasBonusChest() ||
//TODO: different superflat presets for example are not detected, so the cache is not cleared and the world is not generated correctly
                !lastGeneratorOptions.getChunkGenerator().getClass().equals(generatorOptions.getChunkGenerator().getClass())) {
            cache.clear();
            strongholdCache = null;
            lastGeneratorOptions = generatorOptions;
        }
    }
}
