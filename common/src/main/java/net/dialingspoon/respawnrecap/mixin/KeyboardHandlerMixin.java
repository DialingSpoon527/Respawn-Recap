package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapController;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$blockKeyPress(long window, int action, KeyEvent event, CallbackInfo ci) {
        if (RecapController.isActive()) {
            RecapController.keyboardInput(event.key(), action);
            ci.cancel();
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$blockCharacter(long window, CharacterEvent event, CallbackInfo ci) {
        if (RecapController.isActive()) {
            ci.cancel();
        }
    }
}
