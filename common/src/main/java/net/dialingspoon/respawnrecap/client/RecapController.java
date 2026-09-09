package net.dialingspoon.respawnrecap.client;

import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class RecapController {
    private static final long SKIP_GRACE_PERIOD_MILLIS = 500L;
    private static final int HOLD_TO_SKIP_TICKS = 10;
    private static final long DOUBLE_INPUT_WINDOW_MILLIS = 250L;
    private static final long RESPAWN_BREATH_DELAY_MILLIS = 200L;
    private static final long RESPAWN_BLINK_MILLIS = 250L;

    private static State state = State.IDLE;
    private static boolean skipRequested;
    private static int heldInputCount;
    private static int heldInputTicks;
    private static long firstInputPressMillis = -1L;
    private static long respawnBreathAtMillis = -1L;
    private static long respawnBlinkStartMillis = -1L;
    private static boolean worldOpen;

    private RecapController() {
    }

    public static void tick(Minecraft minecraft) {
        if (!minecraft.isWindowActive()) {
            resetSkipInput();
        }
        if (minecraft.level == null || minecraft.player == null) {
            if (worldOpen || state != State.IDLE) {
                close(minecraft);
            }
            return;
        }

        worldOpen = true;
        tickDelayedEffects(minecraft);
        boolean dead = minecraft.player.isDeadOrDying();

        if (state == State.IDLE) {
            if (dead) {
                startDeathReplay(minecraft);
            } else {
                ReplayRecorder.tick(minecraft);
            }
            return;
        }

        if (!dead) {
            finishRespawn(minecraft);
            return;
        }
        if (state == State.RESPAWNING) {
            return;
        }
        if (shouldSkip() || RecapTimeline.elapsedMillis() >= RecapTimeline.durationMillis()) {
            finishReplay(minecraft);
            return;
        }
        RecapSounds.tick(minecraft);
    }

    public static boolean isActive() {
        return state != State.IDLE;
    }

    public static float respawnBlinkProgress() {
        if (respawnBlinkStartMillis < 0L) {
            return 0.0F;
        }
        float progress = 1.0F - (Util.getMillis() - respawnBlinkStartMillis) / (float) RESPAWN_BLINK_MILLIS;
        if (progress > 0.0F) {
            return Math.min(1.0F, progress);
        }
        respawnBlinkStartMillis = -1L;
        return 0.0F;
    }

    public static void keyboardInput(int key, int action) {
        if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS && state == State.DEATH_REPLAY) {
            finishReplay(Minecraft.getInstance());
            return;
        }
        recordInput(action);
    }

    public static void mouseInput(int action) {
        recordInput(action);
    }

    private static void startDeathReplay(Minecraft minecraft) {
        ReplayRecorder.archiveCurrentLife(minecraft);
        state = State.DEATH_REPLAY;
        RecapTimeline.start();
        RecapSounds.start(minecraft);
        KeyMapping.releaseAll();
        resetSkipInput();
    }

    private static void finishReplay(Minecraft minecraft) {
        if (state != State.DEATH_REPLAY || minecraft.player == null) {
            return;
        }
        state = State.RESPAWNING;
        RecapSounds.stop(minecraft);
        resetSkipInput();
        minecraft.player.respawn();
    }

    private static void finishRespawn(Minecraft minecraft) {
        stopPlayback(minecraft);
        ReplayRecorder.beginNewLife(minecraft);
        long now = Util.getMillis();
        respawnBlinkStartMillis = now;
        respawnBreathAtMillis = now + RESPAWN_BREATH_DELAY_MILLIS;
        state = State.IDLE;
    }

    static void close(Minecraft minecraft) {
        stopPlayback(minecraft);
        ReplayRecorder.close(minecraft);
        state = State.IDLE;
        respawnBreathAtMillis = -1L;
        respawnBlinkStartMillis = -1L;
        worldOpen = false;
    }

    private static void stopPlayback(Minecraft minecraft) {
        RecapSounds.stop(minecraft);
        RecapTimeline.stop();
        resetSkipInput();
    }

    private static void tickDelayedEffects(Minecraft minecraft) {
        if (respawnBreathAtMillis >= 0L && Util.getMillis() >= respawnBreathAtMillis) {
            respawnBreathAtMillis = -1L;
            RecapSounds.playRespawnBreath(minecraft);
        }
    }

    private static void recordInput(int action) {
        if (state != State.DEATH_REPLAY) {
            return;
        }
        if (action == GLFW.GLFW_PRESS) {
            heldInputCount++;
            if (RecapTimeline.elapsedMillis() < SKIP_GRACE_PERIOD_MILLIS) {
                return;
            }
            long now = Util.getMillis();
            skipRequested = firstInputPressMillis >= 0L && now - firstInputPressMillis <= DOUBLE_INPUT_WINDOW_MILLIS;
            firstInputPressMillis = skipRequested ? -1L : now;
        } else if (action == GLFW.GLFW_RELEASE) {
            heldInputCount = Math.max(0, heldInputCount - 1);
            if (heldInputCount == 0) {
                heldInputTicks = 0;
            }
        }
    }

    private static boolean shouldSkip() {
        if (skipRequested) {
            return true;
        }
        if (RecapTimeline.elapsedMillis() < SKIP_GRACE_PERIOD_MILLIS) {
            heldInputTicks = 0;
            return false;
        }
        if (heldInputCount == 0) {
            heldInputTicks = 0;
            return false;
        }
        return ++heldInputTicks >= HOLD_TO_SKIP_TICKS;
    }

    private static void resetSkipInput() {
        skipRequested = false;
        heldInputCount = 0;
        heldInputTicks = 0;
        firstInputPressMillis = -1L;
    }

    private enum State {
        IDLE,
        DEATH_REPLAY,
        RESPAWNING
    }
}
