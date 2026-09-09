package net.dialingspoon.respawnrecap.forge;

import net.dialingspoon.respawnrecap.RespawnRecap;
import net.dialingspoon.respawnrecap.client.RecapRenderTypes;
import net.dialingspoon.respawnrecap.client.RespawnRecapConfigScreen;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.io.IOException;
import java.io.UncheckedIOException;

@Mod(value = RespawnRecap.MOD_ID)
public final class RespawnRecapForge {
    public RespawnRecapForge() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(RespawnRecapForge::registerShaders);
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
