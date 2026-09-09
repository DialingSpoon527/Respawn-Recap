package net.dialingspoon.respawnrecap;

import net.minecraft.resources.ResourceLocation;

public final class RespawnRecap {
    public static final String MOD_ID = "respawnrecap";

    private RespawnRecap() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
