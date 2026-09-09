package net.dialingspoon.respawnrecap.client;

import net.minecraft.Util;

final class RecapTimeline {
    static final long START_DELAY_MILLIS = 1000L;
    static final long PLAYBACK_START_MILLIS = 7000L;
    static final long REPLAY_APPEAR_MILLIS = PLAYBACK_START_MILLIS - 1000L;
    static final long MASK_ANIM_DURATION_MILLIS = 7000L;
    static final long MASK_END_MILLIS = START_DELAY_MILLIS + MASK_ANIM_DURATION_MILLIS;
    static final float MASK_START_DISTANCE = 10.0F;
    static final float MASK_END_DISTANCE = -1.0F;
    static final long MASK_CROSSING_MILLIS = START_DELAY_MILLIS + Math.round(
            MASK_START_DISTANCE / (MASK_START_DISTANCE - MASK_END_DISTANCE) * MASK_ANIM_DURATION_MILLIS
    );

    private static final long END_HOLD_MILLIS = 1000L;
    private static final long INITIAL_FRAME_MILLIS = 600L;
    private static final long MIN_FRAME_MILLIS = 60L;
    private static final float FRAME_LENGTH_MULTIPLIER = 0.9F;
    private static final long[] FRAME_END_MILLIS = createFrameSchedule();
    private static long startMillis;

    private RecapTimeline() {
    }

    static void start() {
        startMillis = Util.getMillis();
    }

    static void stop() {
        startMillis = 0L;
    }

    static long elapsedMillis() {
        return startMillis == 0L ? 0L : Util.getMillis() - startMillis;
    }

    static float maskProgress() {
        return fraction(START_DELAY_MILLIS, MASK_END_MILLIS);
    }

    static float playbackProgress() {
        return fraction(REPLAY_APPEAR_MILLIS, durationMillis());
    }

    static float blinkProgress() {
        return fraction(playbackEndMillis(), durationMillis());
    }

    static long durationMillis() {
        return playbackEndMillis() + END_HOLD_MILLIS;
    }

    static long playbackEndMillis() {
        int snapshotCount = ReplayRecorder.snapshotCount();
        if (snapshotCount <= 0) {
            return PLAYBACK_START_MILLIS;
        }
        return PLAYBACK_START_MILLIS + FRAME_END_MILLIS[Math.min(snapshotCount, FRAME_END_MILLIS.length) - 1];
    }

    static int snapshotIndexForElapsed(int snapshotCount) {
        if (snapshotCount <= 0) {
            return -1;
        }
        long playbackElapsed = Math.max(0L, elapsedMillis() - PLAYBACK_START_MILLIS);
        int low = 0;
        int high = Math.min(snapshotCount, FRAME_END_MILLIS.length) - 1;
        while (low < high) {
            int middle = low + high >>> 1;
            if (playbackElapsed < FRAME_END_MILLIS[middle]) {
                high = middle;
            } else {
                low = middle + 1;
            }
        }
        return Math.max(0, snapshotCount - 1 - low);
    }

    private static long[] createFrameSchedule() {
        long[] schedule = new long[ReplayRecorder.MAX_SNAPSHOTS];
        float total = 0.0F;
        for (int frame = 0; frame < schedule.length; frame++) {
            float frameMillis = Math.max(INITIAL_FRAME_MILLIS * (float) Math.pow(FRAME_LENGTH_MULTIPLIER, frame), MIN_FRAME_MILLIS);
            total += frameMillis;
            schedule[frame] = Math.round(total);
        }
        return schedule;
    }

    static float fraction(long start, long end) {
        if (end <= start) {
            return 1.0F;
        }
        return Math.min(1.0F, Math.max(0.0F, (elapsedMillis() - start) / (float) (end - start)));
    }
}
