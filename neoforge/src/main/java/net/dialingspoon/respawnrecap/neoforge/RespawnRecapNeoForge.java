package net.dialingspoon.respawnrecap.neoforge;

import net.dialingspoon.respawnrecap.RespawnRecap;
import net.dialingspoon.respawnrecap.client.RecapRenderTypes;
import net.dialingspoon.respawnrecap.client.RespawnRecapConfigScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.io.IOException;
import java.io.UncheckedIOException;

@Mod(value = RespawnRecap.MOD_ID, dist = Dist.CLIENT)
public final class RespawnRecapNeoForge {
    public RespawnRecapNeoForge(ModContainer container, IEventBus modBus) {
        modBus.addListener(RespawnRecapNeoForge::registerShaders);
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (ignored, parent) -> new RespawnRecapConfigScreen(parent));
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
