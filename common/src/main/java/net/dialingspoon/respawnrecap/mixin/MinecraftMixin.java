package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    private Screen screen;

    @Shadow
    @Final
    public MouseHandler mouseHandler;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$suppressDeathScreen(Screen requestedScreen, CallbackInfo ci) {
        boolean deathScreen = requestedScreen instanceof DeathScreen;
        boolean blockedRecapScreen = RecapController.isActive()
                && requestedScreen != null
                && !(requestedScreen instanceof DisconnectedScreen);
        boolean closeWhileDead = requestedScreen == null && this.player != null && this.player.isDeadOrDying();
        if (!deathScreen && !blockedRecapScreen && !closeWhileDead) {
            return;
        }

        if (this.screen != null) {
            this.screen.removed();
            this.screen = null;
        }
        this.mouseHandler.grabMouse();
        ci.cancel();
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void respawnrecap$tickRecap(CallbackInfo ci) {
        RecapController.tick((Minecraft) (Object) this);
    }
}
