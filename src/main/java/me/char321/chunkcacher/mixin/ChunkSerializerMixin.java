package me.char321.chunkcacher.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.EnumSet;

@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin {

    @ModifyVariable(method = "serialize", at = @At("RETURN"), ordinal = 1)
    private static NbtCompound serializeHeightmaps2(NbtCompound nbtCompound, ServerWorld world, Chunk chunk) {
        if (chunk instanceof ProtoChunk) {
            nbtCompound.put(Heightmap.Type.WORLD_SURFACE_WG.getName(), new NbtLongArray(chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG).asLongArray()));
            nbtCompound.put(Heightmap.Type.OCEAN_FLOOR_WG.getName(), new NbtLongArray(chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG).asLongArray()));
        }
        return nbtCompound;
    }

    @Redirect(method = "deserialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkStatus;getHeightmapTypes()Ljava/util/EnumSet;"))
    private static EnumSet<Heightmap.Type> deserializeHeightmaps(ChunkStatus status) {
        return EnumSet.allOf(Heightmap.Type.class);
    }
}