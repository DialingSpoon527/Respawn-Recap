package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    private Screen screen;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void respawnrecap$suppressDeathScreen(Screen requestedScreen, CallbackInfo ci) {
        boolean deathScreen = requestedScreen instanceof DeathScreen;
        boolean blockedRecapScreen = RecapController.isActive()
                && requestedScreen != null
                && !(requestedScreen instanceof DisconnectedScreen);
        boolean closeWhileDead = requestedScreen == null && this.minecraft.player != null && this.minecraft.player.isDeadOrDying();
        if (!deathScreen && !blockedRecapScreen && !closeWhileDead) {
            return;
        }

        if (this.screen != null) {
            this.screen.removed();
            this.screen = null;
        }
        this.minecraft.mouseHandler.grabMouse();
        ci.cancel();
    }
}
