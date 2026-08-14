package net.dialingspoon.respawnrecap.mixin;

import net.dialingspoon.respawnrecap.client.RecapController;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"))
    private void respawnrecap$tickRecap(CallbackInfo ci) {
        RecapController.tick((Minecraft) (Object) this);
    }
}
