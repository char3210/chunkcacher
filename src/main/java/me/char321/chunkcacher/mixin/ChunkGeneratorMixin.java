package me.char321.chunkcacher.mixin;

import me.char321.chunkcacher.WorldCache;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {

    @Inject(method = "generateStrongholdPositions", at = @At("HEAD"), cancellable = true)
    private void applyCachedStrongholds(CallbackInfoReturnable<CompletableFuture<List<ChunkPos>>> cir) {
        if (WorldCache.shouldCache() && WorldCache.strongholdCache != null) {
            cir.setReturnValue(WorldCache.strongholdCache);
        }
    }

    @Inject(method = "generateStrongholdPositions", at = @At("TAIL"))
    private void cacheStrongholds(CallbackInfoReturnable<CompletableFuture<List<ChunkPos>>> cir) {
        if (WorldCache.shouldCache() && WorldCache.strongholdCache == null) {
            WorldCache.strongholdCache = cir.getReturnValue();
        }
    }
}