package me.char321.chunkcacher.mixin;

import com.mojang.datafixers.util.Either;
import me.char321.chunkcacher.WorldCache;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {

    @Shadow @Final ServerWorld world;

    @Inject(method = "method_17225", at = @At("RETURN"), remap = false)
    private void addToCache(CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        if (WorldCache.shouldCache() && cir.getReturnValue().isDone()) {
            cir.getReturnValue().getNow(null).ifLeft((chunk) -> {
                if (!chunk.getStatus().isAtLeast(ChunkStatus.FEATURES)) {
                    WorldCache.addChunk(chunk.getPos(), chunk, world);
                }
            });
        }
    }

    @ModifyVariable(method = "method_17256", at = @At("STORE"), remap = false)
    private NbtCompound loadFromCache(NbtCompound nbtCompound, ChunkPos pos) {
        if (WorldCache.shouldCache() && nbtCompound == null) {
            return WorldCache.getChunkNbt(pos, world);
        }
        return nbtCompound;
    }
}