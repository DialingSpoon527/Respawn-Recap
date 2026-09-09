package net.dialingspoon.respawnrecap.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class RespawnRecapConfigScreen extends Screen {
    private final Screen parent;

    public RespawnRecapConfigScreen(Screen parent) {
        super(Component.translatable("respawnrecap.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        Button audioButton = Button.builder(audioButtonText(), button -> {
            RecapSounds.setMinecraftyAudio(minecraft, !RecapSounds.isMinecraftyAudioEnabled(minecraft));
            button.setMessage(audioButtonText());
        }).bounds(width / 2 - 100, height / 2 - 10, 200, 20).build();
        addRenderableWidget(audioButton);
        addRenderableWidget(Button.builder(Component.translatable("respawnrecap.config.done"), button -> onClose())
                .bounds(width / 2 - 100, height / 2 + 20, 200, 20)
                .build());
    }

    private Component audioButtonText() {
        return Component.translatable(
                "respawnrecap.config.minecrafty_audio",
                Component.translatable(RecapSounds.isMinecraftyAudioEnabled(minecraft) ? "options.on" : "options.off")
        );
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }
}
