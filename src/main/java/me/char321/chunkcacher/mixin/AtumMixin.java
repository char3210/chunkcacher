package me.char321.chunkcacher.mixin;

import me.char321.chunkcacher.WorldCache;
import me.voidxwalker.autoreset.mixin.OptionsScreenMixin;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(OptionsScreen.class)
public class AtumMixin {
//    @Pseudo
//    @Inject(method="lambda$addAutoResetButton$0", at=@At("HEAD"))
//    private void clearCache() {
//        WorldCache.clearCache();
//    }
}
