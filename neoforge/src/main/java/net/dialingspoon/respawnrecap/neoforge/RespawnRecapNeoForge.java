package net.dialingspoon.respawnrecap.neoforge;

import net.dialingspoon.respawnrecap.RespawnRecap;
import net.dialingspoon.respawnrecap.client.RespawnRecapConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = RespawnRecap.MOD_ID, dist = Dist.CLIENT)
public final class RespawnRecapNeoForge {
    public RespawnRecapNeoForge(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (ignored, parent) -> new RespawnRecapConfigScreen(parent));
    }
}
