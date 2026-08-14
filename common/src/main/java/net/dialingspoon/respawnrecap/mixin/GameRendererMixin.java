package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapRenderer;
import net.dialingspoon.respawnrecap.client.ReplayRecorder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Unique
    private final RecapRenderer respawnrecap$recapRenderer = new RecapRenderer();

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private FeatureRenderDispatcher featureRenderDispatcher;

    @Inject(method = "close", at = @At("RETURN"))
    private void respawnrecap$closeRecapRenderer(CallbackInfo ci) {
        this.respawnrecap$recapRenderer.close();
    }

    @Redirect(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render()V"
            )
    )
    private void respawnrecap$renderGuiThenRecap(GuiRenderer guiRenderer) {
        guiRenderer.render();
        ReplayRecorder.capturePendingFrame(this.minecraft);
        this.respawnrecap$recapRenderer.render(this.minecraft, this.featureRenderDispatcher);
    }
}
