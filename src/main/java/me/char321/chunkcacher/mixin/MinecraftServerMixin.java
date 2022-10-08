package me.char321.chunkcacher.mixin;

import me.char321.chunkcacher.WorldCache;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.world.SaveProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Shadow @Final protected SaveProperties saveProperties;

    @Inject(method = "createWorlds", at = @At("HEAD"))
    public void isGenerating(WorldGenerationProgressListener worldGenerationProgressListener, CallbackInfo ci) {
        WorldCache.isGenerating = true;

        WorldCache.checkGeneratorOptions(saveProperties.getGeneratorOptions());
    }

    @Inject(method = "loadWorld", at = @At("TAIL"))
    public void isNotGenerating(CallbackInfo ci) {
        WorldCache.isGenerating = false;
    }
}
