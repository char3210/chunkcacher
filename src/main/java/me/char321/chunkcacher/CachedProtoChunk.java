package me.char321.chunkcacher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkTickScheduler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.StructureFeature;
import org.jetbrains.annotations.Nullable;

import java.util.BitSet;
import java.util.List;
import java.util.Map;

public class CachedProtoChunk {
    private ChunkPos pos;
    private boolean shouldSave;
    private BiomeArray biomes;
    private Map<Heightmap.Type, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Type.class);
    private ChunkStatus status = ChunkStatus.EMPTY;
    private final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    private final Map<BlockPos, NbtCompound> blockEntityTags = Maps.newHashMap();
    private final ChunkSection[] sections = new ChunkSection[16];
    private final List<NbtCompound> entities = Lists.newArrayList();
    private final List<BlockPos> lightSources = Lists.newArrayList();
    private final ShortList[] postProcessingLists = new ShortList[16];
    private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
    private final Map<StructureFeature<?>, LongSet> structureReferences = Maps.newHashMap();
    private long inhabitedTime;
    private final Map<GenerationStep.Carver, BitSet> carvingMasks = new Object2ObjectArrayMap<GenerationStep.Carver, BitSet>();
    private volatile boolean lightOn;

    public CachedProtoChunk(ProtoChunk chunk) {
        pos = chunk.getPos();
    }

    public ProtoChunk toProtoChunk() {
        return null;
    }
}
