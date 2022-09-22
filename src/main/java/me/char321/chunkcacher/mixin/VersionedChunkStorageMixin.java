package me.char321.chunkcacher.mixin;

import me.char321.chunkcacher.WorldCache;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VersionedChunkStorage.class)
public class VersionedChunkStorageMixin {
//    @Inject(method = "getNbt", at = @At("HEAD"), cancellable = true)
//    public void getFromCache(ChunkPos chunkPos, CallbackInfoReturnable<NbtCompound> cir) {
//        if (WorldCache.shouldUseCache()) {
//            cir.setReturnValue(WorldCache.getChunkNbt(chunkPos));
//        }
//    }
}
