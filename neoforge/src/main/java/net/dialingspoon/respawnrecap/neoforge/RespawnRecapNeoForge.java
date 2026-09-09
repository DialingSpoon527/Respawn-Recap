package net.dialingspoon.respawnrecap.neoforge;

import net.dialingspoon.respawnrecap.RespawnRecap;
import net.dialingspoon.respawnrecap.client.RecapRenderTypes;
import net.dialingspoon.respawnrecap.client.RespawnRecapConfigScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.ConfigScreenHandler;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.io.UncheckedIOException;

@Mod(value = RespawnRecap.MOD_ID)
public final class RespawnRecapNeoForge {
    public RespawnRecapNeoForge(ModContainer container, IEventBus modBus) {
        modBus.addListener(RespawnRecapNeoForge::registerShaders);
        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new RespawnRecapConfigScreen(parent)
                )
        );
    }

    private static void registerShaders(RegisterShadersEvent event) {
        try {
            RecapRenderTypes.registerShaders((id, format, onLoaded) ->
                    event.registerShader(new ShaderInstance(event.getResourceProvider(), id, format), onLoaded));
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to load Respawn Recap shaders", exception);
        }
    }
}
