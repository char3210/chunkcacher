package me.char321.chunkcacher.mixin;

import com.mojang.datafixers.util.Either;
import me.char321.chunkcacher.WorldCache;
import net.minecraft.nbt.CompoundTag;
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

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {

    @Shadow @Final private ServerWorld world;

    @Inject(method = "method_17225", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/WorldGenerationProgressListener;setChunkStatus(Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/chunk/ChunkStatus;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void addToCache(ChunkPos chunkPos, ChunkHolder chunkHolder, ChunkStatus chunkStatus, List list, CallbackInfoReturnable<CompletableFuture> cir, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture) {
        if (WorldCache.shouldCache() && completableFuture.isDone() && !chunkStatus.isAtLeast(ChunkStatus.FEATURES)) {
            completableFuture.getNow(null).ifLeft((chunk) -> WorldCache.addChunk(chunkPos, chunk, world));
        }
    }

    @ModifyVariable(method = "getUpdatedChunkTag", at = @At(value="STORE"))
    public CompoundTag loadFromCache(CompoundTag nbtCompound, ChunkPos pos) {
        if (!WorldCache.shouldCache()) {
            return nbtCompound;
        }

        if (nbtCompound == null) {
            return WorldCache.getChunkNbt(pos, world);
        }
        return nbtCompound;
    }
}
