package me.char321.chunkcacher.mixin;

import com.mojang.datafixers.util.Either;
import me.char321.chunkcacher.WorldCache;
import net.minecraft.nbt.NbtCompound;
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

    @Shadow @Final private MessageListener<ChunkTaskPrioritySystem.Task<Runnable>> worldGenExecutor;

    @Shadow protected abstract void releaseLightTicket(ChunkPos pos);

    @Shadow @Final private ServerWorld world;

    @Shadow @Final private StructureManager structureManager;

    @Shadow @Final private ServerLightingProvider serverLightingProvider;

    @Shadow protected abstract CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> convertToFullChunk(ChunkHolder chunkHolder);

    @Shadow protected abstract CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> getRegion(ChunkPos centerChunk, int margin, IntFunction<ChunkStatus> distanceToStatus);

    @Shadow protected abstract ChunkStatus getRequiredStatusForGeneration(ChunkStatus centerChunkTargetStatus, int distance);

    /**
     * @author
     * @reason
     */
    @Overwrite
    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> upgradeChunk(ChunkHolder holder, ChunkStatus requiredStatus) {
        ChunkPos chunkPos = holder.getPos();
        CompletableFuture<Either<List<Chunk>, ChunkHolder.Unloaded>> completableFuture = this.getRegion(chunkPos, requiredStatus.getTaskMargin(), i -> this.getRequiredStatusForGeneration(requiredStatus, i)) ;
        this.world.getProfiler().visit(() -> "chunkGenerate " + requiredStatus.getId());
        return completableFuture.thenComposeAsync(either -> either.map(list -> {
            try {
                CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> future2 = requiredStatus.runGenerationTask(this.world, this.chunkGenerator, this.structureManager, this.serverLightingProvider, chunk -> this.convertToFullChunk(holder), (List<Chunk>)list);
                if (WorldCache.shouldCache() && future2.isDone() && !requiredStatus.isAtLeast(ChunkStatus.FEATURES)) {
                    future2.getNow(null).ifLeft((chunk) -> {
                        WorldCache.addChunk(chunkPos, chunk, world);
                    });
                }
                this.worldGenerationProgressListener.setChunkStatus(chunkPos, requiredStatus);
                return future2;
            }
            catch (Exception exception) {
                CrashReport crashReport = CrashReport.create(exception, "Exception generating new chunk");
                CrashReportSection crashReportSection = crashReport.addElement("Chunk to be generated");
                crashReportSection.add("Location", String.format("%d,%d", chunkPos.x, chunkPos.z));
                crashReportSection.add("Position hash", ChunkPos.toLong(chunkPos.x, chunkPos.z));
                crashReportSection.add("Generator", this.chunkGenerator);
                throw new CrashException(crashReport);
            }
        }, unloaded -> {
            this.releaseLightTicket(chunkPos);
            return CompletableFuture.completedFuture(Either.right(unloaded));
        }), runnable -> this.worldGenExecutor.send(ChunkTaskPrioritySystem.createMessage(holder, runnable)));
    }

    @ModifyVariable(method = "getUpdatedChunkNbt", at = @At(value="STORE"))
    public NbtCompound loadFromCache(NbtCompound nbtCompound, ChunkPos pos) {
        if (!WorldCache.shouldCache()) {
            return nbtCompound;
        }

        if (nbtCompound == null) {
            return WorldCache.getChunkNbt(pos, world);
        }
        return nbtCompound;
    }
}
