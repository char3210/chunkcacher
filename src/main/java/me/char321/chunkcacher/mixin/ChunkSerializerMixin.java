package me.char321.chunkcacher.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
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
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.EnumSet;
import java.util.Iterator;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @ModifyVariable(method = "serialize", at = @At("RETURN"), ordinal = 2)
    private static NbtCompound serializeHeightmaps2(NbtCompound nbtCompound, ServerWorld world, Chunk chunk) {
        nbtCompound.put(Heightmap.Type.WORLD_SURFACE_WG.getName(), new NbtLongArray((chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG)).asLongArray()));
        nbtCompound.put(Heightmap.Type.OCEAN_FLOOR_WG.getName(), new NbtLongArray((chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG)).asLongArray()));
        return nbtCompound;
    }

    @Redirect(method = "deserialize", at = @At(value = "INVOKE", target = "Ljava/util/EnumSet;iterator()Ljava/util/Iterator;"))
    private static Iterator<Heightmap.Type> deserializeHeightmaps(EnumSet instance) {
        return EnumSet.allOf(Heightmap.Type.class).iterator();
    }
}
