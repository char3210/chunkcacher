package me.char321.chunkcacher.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.Heightmap;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.EnumSet;
import java.util.Iterator;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {
    @Inject(method = "serialize", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void serializeHeightmaps(ServerWorld world, Chunk chunk, CallbackInfoReturnable<CompoundTag> cir, ChunkPos x10, CompoundTag compoundTag, CompoundTag compoundTag2, UpgradeData x, ChunkSection[] x1, ListTag x2, LightingProvider x3, boolean x4, BiomeArray x5, ListTag x6, ListTag x7, TickScheduler x8, TickScheduler x9, CompoundTag compoundTag3) {
        compoundTag3.put(Heightmap.Type.WORLD_SURFACE_WG.getName(), new LongArrayTag((chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG)).asLongArray()));
        compoundTag3.put(Heightmap.Type.OCEAN_FLOOR_WG.getName(), new LongArrayTag((chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG)).asLongArray()));

    }

    @Redirect(method = "deserialize", at = @At(value = "INVOKE", target = "Ljava/util/EnumSet;iterator()Ljava/util/Iterator;"))
    private static Iterator<Heightmap.Type> deserializeHeightmaps(EnumSet instance) {
        return EnumSet.allOf(Heightmap.Type.class).iterator();
    }
}
