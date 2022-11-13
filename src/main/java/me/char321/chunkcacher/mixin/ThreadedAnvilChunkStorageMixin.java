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
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {

    @Shadow @Final ServerWorld world;

    @Inject(method = "method_17225", at = @At(value = "RETURN"))
    private void addToCache(ChunkPos chunkPos, ChunkHolder chunkHolder, ChunkStatus chunkStatus, Executor executor, List<?> list, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture = cir.getReturnValue();
        if (WorldCache.shouldCache() && completableFuture.isDone() && !chunkStatus.isAtLeast(ChunkStatus.FEATURES)) {
            completableFuture.getNow(null).ifLeft((chunk) -> WorldCache.addChunk(chunkPos, chunk, world));
        }
    }

    @ModifyVariable(method = "getUpdatedChunkNbt", at = @At(value="STORE"))
    public NbtCompound loadFromCache(NbtCompound nbtCompound, ChunkPos pos) {
        if (WorldCache.shouldCache() && nbtCompound == null) {
            return WorldCache.getChunkNbt(pos, world);
        }
        return nbtCompound;
    }
}
