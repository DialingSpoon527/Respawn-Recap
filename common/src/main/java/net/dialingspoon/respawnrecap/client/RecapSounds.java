package net.dialingspoon.respawnrecap.client;

import net.dialingspoon.respawnrecap.RespawnRecap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

final class RecapSounds {
    private static final float VOLUME = 0.5F;
    private static final long MAIN_START_MILLIS = RecapTimeline.MASK_CROSSING_MILLIS - 6600L;
    private static final long END_CUE_EARLIEST_MILLIS = MAIN_START_MILLIS + 6145L;
    private static final long END_PORTAL_DURATION_MILLIS = 7068L;
    private static final long NETHER_PORTAL_LEAD_MILLIS = 14001L;
    private static final long NETHER_PORTAL_MINIMUM_REPLAY_MILLIS = 17000L;

    private static final Cue MAIN = new Cue("recap.main");
    private static final Cue END_PORTAL = new Cue("recap.end_portal");
    private static final Cue NETHER_PORTAL = new Cue("recap.end_nether");
    private static boolean gameAudioMuted;

    private RecapSounds() {
    }

    static void start(Minecraft minecraft) {
        stop(minecraft);
        setGameAudioGain(minecraft, 0.0F);
        gameAudioMuted = true;
        MAIN.reset();
        END_PORTAL.reset();
        NETHER_PORTAL.reset();
    }

    static void tick(Minecraft minecraft) {
        long elapsed = RecapTimeline.elapsedMillis();
        long duration = RecapTimeline.durationMillis();
        MAIN.playWhenDue(minecraft, elapsed, MAIN_START_MILLIS);
        END_PORTAL.playWhenDue(minecraft, elapsed, Math.max(END_CUE_EARLIEST_MILLIS, duration - END_PORTAL_DURATION_MILLIS));
        if (duration > NETHER_PORTAL_MINIMUM_REPLAY_MILLIS) {
            NETHER_PORTAL.playWhenDue(minecraft, elapsed, Math.max(END_CUE_EARLIEST_MILLIS, duration - NETHER_PORTAL_LEAD_MILLIS));
        }
    }

    static void stop(Minecraft minecraft) {
        MAIN.stop(minecraft);
        END_PORTAL.stop(minecraft);
        NETHER_PORTAL.stop(minecraft);
        if (gameAudioMuted) {
            setGameAudioGain(minecraft, 1.0F);
            gameAudioMuted = false;
        }
    }

    static void playRespawnBreath(Minecraft minecraft) {
        minecraft.getSoundManager().play(createSound("respawn.breath"));
    }

    private static void setGameAudioGain(Minecraft minecraft, float gain) {
        for (SoundSource source : SoundSource.values()) {
            if (source != SoundSource.UI) {
                minecraft.getSoundManager().updateCategoryVolume(source, gain);
            }
        }
    }

    private static SoundInstance createSound(String id) {
        return new SimpleSoundInstance(
                RespawnRecap.id(id),
                SoundSource.UI,
                VOLUME,
                1.0F,
                RandomSource.create(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0D,
                0.0D,
                0.0D,
                true
        );
    }

    private static final class Cue {
        private final String id;
        private SoundInstance instance;
        private boolean played;

        private Cue(String id) {
            this.id = id;
        }

        private void playWhenDue(Minecraft minecraft, long elapsed, long startMillis) {
            if (played || elapsed < Math.max(0L, startMillis)) {
                return;
            }
            played = true;
            instance = createSound(id);
            minecraft.getSoundManager().play(instance);
        }

        private void stop(Minecraft minecraft) {
            if (instance != null) {
                minecraft.getSoundManager().stop(instance);
                instance = null;
            }
        }

        private void reset() {
            played = false;
        }
    }
}
