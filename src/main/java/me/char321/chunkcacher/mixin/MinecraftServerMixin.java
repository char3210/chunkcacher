package me.char321.chunkcacher.mixin;

import me.char321.chunkcacher.WorldCache;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Inject(method = "createWorlds", at = @At("HEAD"))
    private void isGenerating(CallbackInfo ci) {
        WorldCache.isGenerating = true;
    }

    @Inject(method = "loadWorld", at = @At("TAIL"))
    private void isNotGenerating(CallbackInfo ci) {
        WorldCache.isGenerating = false;
    }
}
