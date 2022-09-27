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
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntFunction;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class ThreadedAnvilChunkStorageMixin {
    @Shadow @Final private WorldGenerationProgressListener worldGenerationProgressListener;

    @Shadow @Final private ChunkGenerator chunkGenerator;


    @Shadow protected abstract void releaseLightTicket(ChunkPos pos);

    @Shadow @Final private ServerWorld world;

    @Shadow @Final private StructureManager structureManager;

    @Shadow @Final private ServerLightingProvider serverLightingProvider;

    @Shadow protected abstract CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> convertToFullChunk(ChunkHolder chunkHolder);

    @Shadow protected abstract ChunkStatus getRequiredStatusForGeneration(ChunkStatus centerChunkTargetStatus, int distance);

    @Shadow protected abstract CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> createChunkRegionFuture(ChunkPos centerChunk, int margin, IntFunction<ChunkStatus> distanceToStatus);

    @Shadow @Final private MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> worldgenExecutor;

    /**
     * @author
     * @reason
     */
    @Overwrite
    public CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> method_20617(ChunkHolder chunkHolder, ChunkStatus chunkStatus) { //generateChunk
        ChunkPos chunkPos = chunkHolder.getPos();
        CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> completableFuture = this.createChunkRegionFuture(chunkPos, chunkStatus.getTaskMargin(), (i) -> {
            return this.getRequiredStatusForGeneration(chunkStatus, i);
        });
        this.world.getProfiler().visit(() -> {
            return "chunkGenerate " + chunkStatus.getId();
        });
        return completableFuture.thenComposeAsync((either) -> {
            return either.map((list) -> {
                try {
                    CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> future2 = chunkStatus.runTask(this.world, this.chunkGenerator, this.structureManager, this.serverLightingProvider, (chunk) -> {
                        return this.convertToFullChunk(chunkHolder);
                    }, list);
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
                    crashReportSection.add("Location", (Object)String.format("%d,%d", chunkPos.x, chunkPos.z));
                    crashReportSection.add("Position hash", (Object)ChunkPos.toLong(chunkPos.x, chunkPos.z));
                    crashReportSection.add("Generator", (Object)this.chunkGenerator);
                    throw new CrashException(crashReport);
                }
            }, (unloaded) -> {
                this.releaseLightTicket(chunkPos);
                return CompletableFuture.completedFuture(Either.right(unloaded));
            });
        }, (runnable) -> {
            this.worldgenExecutor.send(ChunkTaskPrioritySystem.createMessage(chunkHolder, runnable));
        });
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
