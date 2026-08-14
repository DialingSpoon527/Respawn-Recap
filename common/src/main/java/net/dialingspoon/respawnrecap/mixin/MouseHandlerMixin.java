package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapController;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    @Inject(method = "onButton", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$blockButton(long window, MouseButtonInfo button, int action, CallbackInfo ci) {
        if (RecapController.isActive()) {
            RecapController.mouseInput(action);
            ci.cancel();
        }
    }

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$blockScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (RecapController.isActive()) {
            ci.cancel();
        }
    }

    @Inject(method = "turnPlayer", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$blockTurning(double frameTime, CallbackInfo ci) {
        if (RecapController.isActive()) {
            ci.cancel();
        }
    }
}
