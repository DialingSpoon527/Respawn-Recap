package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapRenderer;
import net.dialingspoon.respawnrecap.client.ReplayRecorder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Unique
    private final RecapRenderer respawnrecap$recapRenderer = new RecapRenderer();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "close", at = @At("RETURN"))
    private void respawnrecap$closeRecapRenderer(CallbackInfo ci) {
        this.respawnrecap$recapRenderer.close();
    }

    @Inject(
            method = "render",
            at = @At(
                    value = "CONSTANT",
                    args = "stringValue=toasts",
                    shift = At.Shift.BEFORE
            )
    )
    private void respawnrecap$renderRecapAfterGui(
            float f, long l, boolean bl, CallbackInfo ci
    ) {
        ReplayRecorder.capturePendingFrame(this.minecraft);
        this.respawnrecap$recapRenderer.render(
                this.minecraft
        );
    }
}
