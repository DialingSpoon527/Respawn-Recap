package net.dialingspoon.respawnrecap.client;

import com.luciad.imageio.webp.CompressionType;
import com.luciad.imageio.webp.WebPWriteParam;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.dialingspoon.respawnrecap.mixin.MinecraftServerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.Util;
import org.slf4j.Logger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Stream;

public final class ReplayRecorder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int CAPTURE_INTERVAL_TICKS = 100;
    private static final int SNAPSHOT_HEIGHT = 270;
    private static final String SNAPSHOT_PREFIX = "snapshot_";
    private static final String SNAPSHOT_SUFFIX = ".webp";
    static final int MAX_SNAPSHOTS = 300;
    private static final Object ARCHIVE_LOCK = new Object();
    private static final List<SnapshotFile> ACTIVE_SNAPSHOTS = new ArrayList<>();
    private static final List<NativeImage> PLAYBACK_FRAMES = new ArrayList<>();

    private static int ticksUntilCapture = 1;
    private static volatile boolean captureInFlight;
    private static boolean captureRequested;
    private static volatile long archiveGeneration;
    private static String loadedArchiveKey;
    private static int playbackFrameCount;
    private static long nextSnapshotId;
    private static DynamicTexture displayTexture;
    private static NativeImage displayPixels;
    private static NativeImage displayedFrame;

    private ReplayRecorder() {
    }

    static void tick(Minecraft minecraft) {
        ensureArchiveScope(minecraft);
        if (captureInFlight || captureRequested || --ticksUntilCapture > 0) {
            return;
        }
        ticksUntilCapture = CAPTURE_INTERVAL_TICKS;
        captureRequested = true;
    }

    public static void capturePendingFrame(Minecraft minecraft) {
        if (!captureRequested
                || captureInFlight
                || minecraft.level == null
                || minecraft.player == null
                || minecraft.player.isDeadOrDying()) {
            return;
        }
        captureRequested = false;
        captureInFlight = true;
        long generation = archiveGeneration;
        Path directory = currentReplayDirectory(minecraft);
        Screenshot.takeScreenshot(
                minecraft.gameRenderer.mainRenderTarget(),
                image -> Util.ioPool().execute(() -> saveSnapshot(image, generation, directory))
        );
    }

    static void archiveCurrentLife(Minecraft minecraft) {
        ensureArchiveScope(minecraft);
        Path backup = backupReplayDirectory(minecraft);
        Path pendingBackup = replayRoot(minecraft).resolve("backup_pending");
        long generation;
        List<Path> playbackPaths;
        synchronized (ARCHIVE_LOCK) {
            archiveGeneration++;
            generation = archiveGeneration;
            captureRequested = false;
            Path current = currentReplayDirectory(minecraft);
            Path archivedDirectory = current;
            boolean currentArchived = false;
            try {
                finishPendingArchive(pendingBackup, backup);
                if (Files.isDirectory(current)) {
                    Files.move(current, pendingBackup);
                    archivedDirectory = pendingBackup;
                    currentArchived = true;
                    deleteDirectory(backup);
                    Files.move(pendingBackup, backup);
                    archivedDirectory = backup;
                } else {
                    deleteDirectory(backup);
                    Files.createDirectories(backup);
                    archivedDirectory = backup;
                }
                playbackPaths = snapshotPathsIn(archivedDirectory);
                ACTIVE_SNAPSHOTS.clear();
                nextSnapshotId = 0L;
            } catch (IOException exception) {
                LOGGER.warn("Failed to archive replay snapshots", exception);
                if (currentArchived) {
                    playbackPaths = snapshotPathsIn(archivedDirectory);
                    ACTIVE_SNAPSHOTS.clear();
                    nextSnapshotId = 0L;
                } else {
                    playbackPaths = ACTIVE_SNAPSHOTS.stream().map(SnapshotFile::path).toList();
                }
            }
            try {
                Files.createDirectories(current);
            } catch (IOException exception) {
                LOGGER.warn("Failed to create replay snapshot directory", exception);
            }
        }
        clearPlayback(minecraft);
        playbackFrameCount = playbackPaths.size();
        if (!playbackPaths.isEmpty()) {
            loadFramesAsync(minecraft, playbackPaths, generation);
        }
    }

    private static void finishPendingArchive(Path pendingBackup, Path backup) throws IOException {
        if (!Files.exists(pendingBackup)) {
            return;
        }
        deleteDirectory(backup);
        Files.move(pendingBackup, backup);
    }

    private static List<Path> snapshotPathsIn(Path directory) {
        return ACTIVE_SNAPSHOTS.stream()
                .map(snapshot -> directory.resolve(snapshot.path().getFileName()))
                .toList();
    }

    static void beginNewLife(Minecraft minecraft) {
        synchronized (ARCHIVE_LOCK) {
            archiveGeneration++;
            captureRequested = false;
            try {
                Files.createDirectories(currentReplayDirectory(minecraft));
            } catch (IOException exception) {
                LOGGER.warn("Failed to create replay snapshot directory", exception);
            }
        }
        clearPlayback(minecraft);
        ticksUntilCapture = 1;
    }

    static boolean prepareCurrentFrame(Minecraft minecraft) {
        int index = RecapTimeline.snapshotIndexForElapsed(PLAYBACK_FRAMES.size());
        return index >= 0 && uploadFrame(minecraft, PLAYBACK_FRAMES.get(index));
    }

    static int snapshotCount() {
        return playbackFrameCount;
    }

    static float replayAspectRatio() {
        return displayPixels == null ? 16.0F / 9.0F : displayPixels.getWidth() / (float) displayPixels.getHeight();
    }

    static void close(Minecraft minecraft) {
        synchronized (ARCHIVE_LOCK) {
            archiveGeneration++;
            captureRequested = false;
        }
        clearPlayback(minecraft);
        loadedArchiveKey = null;
        ACTIVE_SNAPSHOTS.clear();
        nextSnapshotId = 0L;
    }

    private static void saveSnapshot(NativeImage screenshot, long generation, Path directory) {
        try (screenshot) {
            synchronized (ARCHIVE_LOCK) {
                if (generation == archiveGeneration) {
                    try (NativeImage resized = resize(screenshot)) {
                        Files.createDirectories(directory);
                        long snapshotId = nextSnapshotId++;
                        Path target = directory.resolve(SNAPSHOT_PREFIX + snapshotId + SNAPSHOT_SUFFIX);
                        writeWebP90(resized, target);
                        ACTIVE_SNAPSHOTS.add(new SnapshotFile(target, snapshotId));
                        trimArchive();
                    }
                }
            }
        } catch (Exception exception) {
            LOGGER.warn("Failed to save replay snapshot", exception);
        } finally {
            captureInFlight = false;
        }
    }

    public static void writeWebP90(NativeImage image, Path path) throws IOException {
        int[] pixels = image.getPixels();

        int width = image.getWidth();
        int height = image.getHeight();

        BufferedImage buffered = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        buffered.setRGB(0, 0, width, height, pixels, 0, width);

        ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
        WebPWriteParam params = (WebPWriteParam) writer.getDefaultWriteParam();
        params.setCompressionType(CompressionType.Lossy);
        params.setCompressionQuality(0.90f);

        try (FileImageOutputStream output = new FileImageOutputStream(path.toFile())) {
            writer.setOutput(output);
            writer.write(null, new IIOImage(buffered, null, null), params);
        } finally {
            writer.dispose();
        }
    }

    private static void ensureArchiveScope(Minecraft minecraft) {
        String archiveKey = archiveKey(minecraft);
        if (archiveKey.equals(loadedArchiveKey)) {
            return;
        }
        synchronized (ARCHIVE_LOCK) {
            archiveGeneration++;
            captureRequested = false;
        }
        clearPlayback(minecraft);
        loadedArchiveKey = archiveKey;
        ticksUntilCapture = 1;
        Path directory = currentReplayDirectory(minecraft);
        synchronized (ARCHIVE_LOCK) {
            ACTIVE_SNAPSHOTS.clear();
            if (Files.isDirectory(directory)) {
                try (Stream<Path> files = Files.list(directory)) {
                    files.map(SnapshotFile::parse)
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparingLong(SnapshotFile::id))
                            .forEach(ACTIVE_SNAPSHOTS::add);
                } catch (IOException exception) {
                    LOGGER.warn("Failed to index replay snapshots", exception);
                }
            }
            nextSnapshotId = ACTIVE_SNAPSHOTS.isEmpty() ? 0L : ACTIVE_SNAPSHOTS.getLast().id() + 1L;
            try {
                trimArchive();
            } catch (IOException exception) {
                LOGGER.warn("Failed to trim replay snapshots", exception);
            }
        }
    }

    private static void loadFramesAsync(Minecraft minecraft, List<Path> paths, long generation) {
        Util.ioPool().execute(() -> {
            List<NativeImage> loadedFrames = new ArrayList<>(paths.size());
            for (Path path : paths) {
                if (generation != archiveGeneration) {
                    closeImages(loadedFrames);
                    return;
                }
                try {
                    BufferedImage buffered = ImageIO.read(path.toFile());
                    NativeImage image = toNativeImage(buffered);
                    loadedFrames.add(flippedCopy(image));
                } catch (IOException exception) {
                    LOGGER.warn("Failed to load replay snapshot {}", path, exception);
                }
            }
            if (generation != archiveGeneration) {
                closeImages(loadedFrames);
                return;
            }
            minecraft.execute(() -> acceptLoadedFrames(loadedFrames, generation));
        });
    }

    private static NativeImage toNativeImage(BufferedImage buffered) {
        int width = buffered.getWidth();
        int height = buffered.getHeight();

        int[] pixels = ((DataBufferInt) buffered.getRaster().getDataBuffer()).getData();

        NativeImage image = new NativeImage(width, height, true);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setPixel(x, y, pixels[y * width + x]);
            }
        }

        return image;
    }

    private static void acceptLoadedFrames(List<NativeImage> loadedFrames, long generation) {
        if (generation != archiveGeneration) {
            closeImages(loadedFrames);
            return;
        }
        closeFrames();
        PLAYBACK_FRAMES.addAll(loadedFrames);
        playbackFrameCount = PLAYBACK_FRAMES.size();
        displayedFrame = null;
    }

    private static boolean uploadFrame(Minecraft minecraft, NativeImage frame) {
        if (frame == displayedFrame) {
            return true;
        }
        if (displayPixels == null || displayPixels.getWidth() != frame.getWidth() || displayPixels.getHeight() != frame.getHeight()) {
            releaseDisplayTexture(minecraft);
            displayPixels = new NativeImage(frame.format(), frame.getWidth(), frame.getHeight(), false);
            displayTexture = new DynamicTexture(() -> "Respawn Recap replay frame", displayPixels);
            minecraft.getTextureManager().register(RecapRenderTypes.REPLAY_TEXTURE, displayTexture);
        }
        displayPixels.copyFrom(frame);
        displayTexture.upload();
        displayedFrame = frame;
        return true;
    }

    private static NativeImage resize(NativeImage image) {
        int width = Math.max(1, Math.round(SNAPSHOT_HEIGHT * image.getWidth() / (float) image.getHeight()));
        NativeImage resized = new NativeImage(image.format(), width, SNAPSHOT_HEIGHT, false);
        image.resizeSubRectTo(0, 0, image.getWidth(), image.getHeight(), resized);
        return resized;
    }

    private static NativeImage flippedCopy(NativeImage image) {
        NativeImage copy = new NativeImage(image.format(), image.getWidth(), image.getHeight(), false);
        image.copyRect(copy, 0, 0, 0, 0, image.getWidth(), image.getHeight(), true, true);
        return copy;
    }

    private static void trimArchive() throws IOException {
        while (ACTIVE_SNAPSHOTS.size() > MAX_SNAPSHOTS) {
            Files.deleteIfExists(ACTIVE_SNAPSHOTS.remove(snapshotRemovalIndex(ACTIVE_SNAPSHOTS)).path());
        }
    }

    private static int snapshotRemovalIndex(List<SnapshotFile> snapshots) {
        int last = snapshots.size() - 1;
        int selected = 1;
        double selectedScore = Double.POSITIVE_INFINITY;
        double selectedCenterDistance = Double.POSITIVE_INFINITY;
        for (int index = 1; index < last; index++) {
            double position = index / (double) last;
            double centerDistance = Math.abs(position - 0.5D);
            double edgeDistance = centerDistance * 2.0D;
            double edgeProtection = 1.0D + 4.0D * edgeDistance * edgeDistance;
            long mergedGap = Math.max(1L, snapshots.get(index + 1).id() - snapshots.get(index - 1).id());
            double score = mergedGap * edgeProtection * snapshotScoreJitter(snapshots.get(index).id());
            if (score < selectedScore || score == selectedScore && centerDistance < selectedCenterDistance) {
                selected = index;
                selectedScore = score;
                selectedCenterDistance = centerDistance;
            }
        }
        return selected;
    }

    private static double snapshotScoreJitter(long snapshotId) {
        long hash = snapshotId;
        hash = (hash ^ hash >>> 33) * 0xFF51AFD7ED558CCDL;
        hash = (hash ^ hash >>> 33) * 0xC4CEB9FE1A85EC53L;
        hash ^= hash >>> 33;
        double normalized = (hash >>> 11) * 0x1.0P-53;
        return 0.97D + normalized * 0.06D;
    }

    private static void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(path);
            }
        }
    }

    private static Path replayRoot(Minecraft minecraft) {
        return minecraft.gameDirectory.toPath()
                .resolve("respawnrecap")
                .resolve("replay")
                .resolve(archiveKey(minecraft));
    }

    private static String archiveKey(Minecraft minecraft) {
        if (minecraft.hasSingleplayerServer() && minecraft.getSingleplayerServer() != null) {
            String levelId = ((MinecraftServerAccessor) minecraft.getSingleplayerServer()).respawnrecap$storageSource().getLevelId();
            return scopedKey("world", levelId);
        }
        ServerData server = minecraft.getCurrentServer();
        String address = server == null || server.ip == null ? "unknown" : server.ip.toLowerCase(Locale.ROOT);
        return scopedKey("server", address);
    }

    private static String scopedKey(String type, String identity) {
        String slug = identity.replaceAll("[^a-zA-Z0-9._-]+", "_");
        if (slug.isBlank()) {
            slug = "unnamed";
        } else if (slug.length() > 48) {
            slug = slug.substring(0, 48);
        }
        return type + "_" + slug + "_" + identityHash(identity);
    }

    private static String identityHash(String identity) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(identity.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static Path currentReplayDirectory(Minecraft minecraft) {
        return replayRoot(minecraft).resolve("current");
    }

    private static Path backupReplayDirectory(Minecraft minecraft) {
        return replayRoot(minecraft).resolve("backup");
    }

    private static void closeFrames() {
        closeImages(PLAYBACK_FRAMES);
        PLAYBACK_FRAMES.clear();
    }

    private static void closeImages(Iterable<NativeImage> images) {
        images.forEach(NativeImage::close);
    }

    private static void releaseDisplayTexture(Minecraft minecraft) {
        if (displayTexture != null) {
            minecraft.getTextureManager().release(RecapRenderTypes.REPLAY_TEXTURE);
        }
        displayTexture = null;
        displayPixels = null;
        displayedFrame = null;
    }

    private static void clearPlayback(Minecraft minecraft) {
        closeFrames();
        releaseDisplayTexture(minecraft);
        playbackFrameCount = 0;
    }

    private record SnapshotFile(Path path, long id) {
        private static SnapshotFile parse(Path path) {
            String name = path.getFileName().toString();
            if (!name.startsWith(SNAPSHOT_PREFIX) || !name.endsWith(SNAPSHOT_SUFFIX)) {
                return null;
            }
            String id = name.substring(SNAPSHOT_PREFIX.length(), name.length() - SNAPSHOT_SUFFIX.length());
            if (id.isEmpty() || !id.chars().allMatch(Character::isDigit)) {
                return null;
            }
            try {
                return new SnapshotFile(path, Long.parseLong(id));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }
}
