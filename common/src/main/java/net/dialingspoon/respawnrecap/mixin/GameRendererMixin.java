package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapRenderer;
import net.dialingspoon.respawnrecap.client.ReplayRecorder;
import net.minecraft.client.DeltaTracker;
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
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderSavingIndicator(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V",
                    shift = At.Shift.BEFORE
            )
    )
    private void respawnrecap$renderRecapAfterGui(
            DeltaTracker deltaTracker, boolean advanceGameTime, CallbackInfo ci
    ) {
        ReplayRecorder.capturePendingFrame(this.minecraft);
        this.respawnrecap$recapRenderer.render(
                this.minecraft
        );
    }
}
