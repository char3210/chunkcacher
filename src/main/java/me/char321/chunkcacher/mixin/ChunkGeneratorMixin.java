package me.char321.chunkcacher.mixin;

import me.char321.chunkcacher.WorldCache;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkGenerator.class)
public class ChunkGeneratorMixin {
    @Mutable
    @Shadow @Final private List<ChunkPos> strongholds;

    @Inject(method = "generateStrongholdPositions", at = @At("HEAD"))
    private void applyCachedStrongholds(CallbackInfo ci) {
        if (WorldCache.strongholdCache != null && this.strongholds.isEmpty()) {
            this.strongholds = WorldCache.strongholdCache;
        }
    }

    @Inject(method = "generateStrongholdPositions", at = @At("TAIL"))
    private void cacheStrongholds(CallbackInfo ci) {
        if (WorldCache.strongholdCache == null) {
            WorldCache.strongholdCache = this.strongholds;
        }
    }
}
