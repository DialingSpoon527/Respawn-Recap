package net.dialingspoon.respawnrecap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;

public final class RespawnRecapFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CoreShaderRegistrationCallback.EVENT.register(context -> RecapRenderTypes.registerShaders(context::register));
    }
}
