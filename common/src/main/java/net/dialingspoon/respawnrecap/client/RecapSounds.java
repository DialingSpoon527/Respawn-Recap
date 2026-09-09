package net.dialingspoon.respawnrecap.client;

import net.dialingspoon.respawnrecap.RespawnRecap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class RecapSounds {
    private static final String AUDIO_CONFIG_KEY = "minecrafty_audio";
    private static final float VOLUME = 0.5F;
    private static final long MAIN_START_MILLIS = RecapTimeline.MASK_CROSSING_MILLIS - 6600L;
    private static final long END_CUE_EARLIEST_MILLIS = MAIN_START_MILLIS + 6145L;
    private static final long END_PORTAL_DURATION_MILLIS = 7068L;
    private static final long OUTER_WILDS_END_STINGER_MILLIS = 3500L;
    private static final long OUTER_WILDS_FADE_OUT_MILLIS = 2000L;
    private static final long NETHER_PORTAL_LEAD_MILLIS = 14001L;
    private static final long NETHER_PORTAL_MINIMUM_REPLAY_MILLIS = 17000L;

    private static final Cue MAIN = new Cue("recap.main");
    private static final Cue END_PORTAL = new Cue("recap.end_portal");
    private static final Cue NETHER_PORTAL = new Cue("recap.end_nether");
    private static final Cue OUTER_WILDS_BASE = new Cue("ow.base");
    private static final Cue OUTER_WILDS_OVERLAY = new Cue("ow.overlay");
    private static final Cue OUTER_WILDS_END = new Cue("ow.end");
    private static boolean minecraftyAudio;
    private static long outerWildsFadeStartMillis = -1L;

    private RecapSounds() {
    }

    static void start(Minecraft minecraft) {
        stop(minecraft);
        minecraftyAudio = isMinecraftyAudioEnabled(minecraft);
        minecraft.getSoundManager().stop();
        MAIN.reset();
        END_PORTAL.reset();
        NETHER_PORTAL.reset();
        OUTER_WILDS_BASE.reset();
        OUTER_WILDS_OVERLAY.reset();
        OUTER_WILDS_END.reset();
        outerWildsFadeStartMillis = -1L;
    }

    static void tick(Minecraft minecraft) {
        long elapsed = RecapTimeline.elapsedMillis();
        long duration = RecapTimeline.durationMillis();
        if (!minecraftyAudio) {
            tickOuterWilds(minecraft, elapsed, duration);
            return;
        }
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
        OUTER_WILDS_BASE.stop(minecraft);
        OUTER_WILDS_OVERLAY.stop(minecraft);
        OUTER_WILDS_END.stop(minecraft);
    }

    static void playRespawnBreath(Minecraft minecraft) {
        minecraft.getSoundManager().play(createSound("respawn.breath"));
    }

    static boolean isMinecraftyAudioEnabled(Minecraft minecraft) {
        Path config = minecraft.gameDirectory.toPath().resolve("config").resolve("respawnrecap.properties");
        Properties properties = new Properties();
        try {
            Files.createDirectories(config.getParent());
            if (Files.notExists(config)) {
                Files.writeString(config, AUDIO_CONFIG_KEY + "=false\n", StandardCharsets.UTF_8);
                return false;
            }
            try (Reader reader = Files.newBufferedReader(config, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException ignored) {
            return false;
        }
        return Boolean.parseBoolean(properties.getProperty(AUDIO_CONFIG_KEY, "false"));
    }

    static void setMinecraftyAudio(Minecraft minecraft, boolean enabled) {
        Path config = minecraft.gameDirectory.toPath().resolve("config").resolve("respawnrecap.properties");
        try {
            Files.createDirectories(config.getParent());
            Files.writeString(config, AUDIO_CONFIG_KEY + "=" + enabled + "\n", StandardCharsets.UTF_8);
            minecraftyAudio = enabled;
        } catch (IOException ignored) {
        }
    }

    private static SoundInstance createSound(String id) {
        return new SimpleSoundInstance(
                RespawnRecap.id(id),
                SoundSource.RECORDS,
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

    private static void tickOuterWilds(Minecraft minecraft, long elapsed, long duration) {
        if (elapsed < RecapTimeline.START_DELAY_MILLIS) {
            return;
        }
        OUTER_WILDS_BASE.playLoopWhenDue(minecraft, elapsed, RecapTimeline.START_DELAY_MILLIS, VOLUME);
        OUTER_WILDS_OVERLAY.playLoopWhenDue(minecraft, elapsed, RecapTimeline.START_DELAY_MILLIS, 0.0F);

        float overlayVolume = 0.0F;
        if (elapsed >= RecapTimeline.PLAYBACK_START_MILLIS) {
            long endStingerAt = duration - OUTER_WILDS_END_STINGER_MILLIS;
            long overlayStart = RecapTimeline.PLAYBACK_START_MILLIS
                    + (endStingerAt - RecapTimeline.PLAYBACK_START_MILLIS) / 2L;
            overlayVolume = inverseLerp(overlayStart, endStingerAt, elapsed);
            OUTER_WILDS_END.playWhenDue(minecraft, elapsed, endStingerAt);
            if (outerWildsFadeStartMillis < 0L && elapsed >= duration - OUTER_WILDS_FADE_OUT_MILLIS) {
                outerWildsFadeStartMillis = elapsed;
            }
        }

        float fade = outerWildsFadeStartMillis < 0L
                ? 1.0F
                : 1.0F - inverseLerp(outerWildsFadeStartMillis, outerWildsFadeStartMillis + OUTER_WILDS_FADE_OUT_MILLIS, elapsed);
        OUTER_WILDS_BASE.setVolume(VOLUME * fade);
        OUTER_WILDS_OVERLAY.setVolume(VOLUME * overlayVolume * fade);
    }

    private static float inverseLerp(long start, long end, long value) {
        if (start == end) {
            return 0.0F;
        }
        return Mth.clamp((value - start) / (float) (end - start), 0.0F, 1.0F);
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

        private void playLoopWhenDue(Minecraft minecraft, long elapsed, long startMillis, float volume) {
            if (played || elapsed < startMillis) {
                return;
            }
            played = true;
            instance = new LoopSound(RespawnRecap.id(id), volume);
            minecraft.getSoundManager().play(instance);
        }

        private void setVolume(float volume) {
            if (instance instanceof LoopSound sound) {
                sound.setVolume(volume);
            }
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

    private static final class LoopSound extends AbstractTickableSoundInstance {
        private LoopSound(ResourceLocation id, float volume) {
            super(SoundEvent.createVariableRangeEvent(id), SoundSource.RECORDS, RandomSource.create());
            this.looping = true;
            this.relative = true;
            this.attenuation = Attenuation.NONE;
            this.volume = volume;
        }

        private void setVolume(float volume) {
            this.volume = volume;
        }

        @Override
        public boolean canStartSilent() {
            return true;
        }

        @Override
        public void tick() {
        }
    }
}
