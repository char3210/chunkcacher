package me.char321.chunkcacher.mixin;

import me.char321.chunkcacher.WorldCache;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TitleScreenMixin {
    @Inject(method = "init", at = @At("HEAD"))
    public void clearCacheOnAtumQuit(CallbackInfo ci) {
        if (!Atum.isRunning) {
            WorldCache.clearCache();
        }
    }
}
