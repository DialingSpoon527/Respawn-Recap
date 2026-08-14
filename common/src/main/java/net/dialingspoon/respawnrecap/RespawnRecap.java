package net.dialingspoon.respawnrecap;

import net.minecraft.resources.Identifier;

public final class RespawnRecap {
    public static final String MOD_ID = "respawnrecap";

    private RespawnRecap() {
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
