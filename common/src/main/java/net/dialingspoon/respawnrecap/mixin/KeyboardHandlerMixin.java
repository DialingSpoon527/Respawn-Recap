package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapController;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$blockKeyPress(long l, int i, int j, int k, int m, CallbackInfo ci) {
        if (RecapController.isActive()) {
            RecapController.keyboardInput(i, k);
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$blockCharacter(long l, int i, int j, CallbackInfo ci) {
        if (RecapController.isActive()) {
            ci.cancel();
        }
    }
}
