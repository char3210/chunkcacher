package me.char321.chunkcacher.mixin;

import com.mojang.datafixers.util.Either;
import me.char321.chunkcacher.WorldCache;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTaskPrioritySystem;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.MessageListener;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.chunk.ChunkGenerator;
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
    @Shadow @Final private WorldGenerationProgressListener worldGenerationProgressListener;

    @Shadow @Final private ChunkGenerator chunkGenerator;

    @Shadow @Final private MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> worldGenExecutor;

    @Shadow protected abstract void releaseLightTicket(ChunkPos pos);

    @Shadow @Final private ServerWorld world;

    @Shadow @Final private StructureManager structureManager;

    @Shadow @Final private ServerLightingProvider serverLightingProvider;

    @Shadow protected abstract CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> convertToFullChunk(ChunkHolder chunkHolder);

    @Inject(method = "generateChunk", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    public void storeIntoCache(ChunkHolder chunkHolder, ChunkStatus chunkStatus, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir, ChunkPos chunkPos, CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> completableFuture) {
        cir.setReturnValue(
        completableFuture.thenComposeAsync(
                either -> either.map(
                        list -> {
                            try {
                                CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> future2 = chunkStatus.runGenerationTask(
                                        this.world, this.chunkGenerator, this.structureManager, this.serverLightingProvider, chunk -> this.convertToFullChunk(chunkHolder), list
                                );
                                if (WorldCache.shouldCache() && future2.isDone() && !chunkStatus.isAtLeast(ChunkStatus.FEATURES)) {
                                    future2.getNow(null).ifLeft((chunk) -> {
                                        WorldCache.addChunk(chunkPos, chunk, world);
                                    });
                                }
                                this.worldGenerationProgressListener.setChunkStatus(chunkPos, chunkStatus);
                                return future2;
                            } catch (Exception var8) {
                                CrashReport crashReport = CrashReport.create(var8, "Exception generating new chunk");
                                CrashReportSection crashReportSection = crashReport.addElement("Chunk to be generated");
                                crashReportSection.add("Location", String.format("%d,%d", chunkPos.x, chunkPos.z));
                                crashReportSection.add("Position hash", ChunkPos.toLong(chunkPos.x, chunkPos.z));
                                crashReportSection.add("Generator", this.chunkGenerator);
                                throw new CrashException(crashReport);
                            }
                        },
                        unloaded -> {
                            this.releaseLightTicket(chunkPos);
                            return CompletableFuture.completedFuture(Either.right(unloaded));
                        }
                ),
                runnable -> this.worldGenExecutor.send(ChunkTaskPrioritySystem.createMessage(chunkHolder, runnable))
        )
        );
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
