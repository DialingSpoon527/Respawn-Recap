package net.dialingspoon.respawnrecap.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.dialingspoon.respawnrecap.client.model.MaskModel;
import net.dialingspoon.respawnrecap.client.model.MemoryStreamModel;
import net.dialingspoon.respawnrecap.client.model.ReplayPlaneModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

final class RecapScene {
    private static final ReplayPlaneModel REPLAY_PLANE = ReplayPlaneModel.create();
    private static final MemoryStreamModel STREAM_MODEL = MemoryStreamModel.create();
    private static final MaskModel MASK_MODEL = MaskModel.create();
    private static final EntityRenderState RENDER_STATE = new EntityRenderState();
    private static final PoseStack POSE = new PoseStack();

    private static final float REPLAY_START_DISTANCE = 1.4F;
    private static final float REPLAY_END_DISTANCE = 0.1F;
    private static final float REPLAY_WIDTH = 5.5F;
    private static final float OCCLUDER_SCALE = 900.0F;
    private static final float MASK_Y_OFFSET = -1.34F;
    private static final long MASK_BRIGHTEN_START_MILLIS = 3000L;
    private static final float MASK_MAX_BRIGHTNESS = 0.5F;
    private static final float STREAM_XY_SCALE = 2.0F / 64.0F;
    private static final float STREAM_Z_SCALE = 0.15F;
    private static final float STREAM_SPEED = 0.5F;
    private static final int STREAM_TINT = 0xFF6E7DFF;
    private static final StreamPlacement[] STREAMS = {
            new StreamPlacement(0.0F, -2.75F),
            new StreamPlacement(120.0F, -4.0F),
            new StreamPlacement(240.0F, -3.0F)
    };

    private RecapScene() {
    }

    static void submitEffects(Minecraft minecraft, SubmitNodeCollector collector) {
        submitReplay(minecraft, collector);
        submitStreams(collector);
    }

    static void submitOccluder(SubmitNodeCollector collector) {
        POSE.setIdentity();
        POSE.translate(0.0F, 0.0F, maskZ());
        POSE.scale(OCCLUDER_SCALE, OCCLUDER_SCALE, 1.0F);
        submit(REPLAY_PLANE, collector, RecapRenderTypes.OCCLUDER, 0xFF000000);
    }

    static void submitMask(SubmitNodeCollector collector) {
        POSE.setIdentity();
        POSE.translate(0.0F, 0.0F, maskZ());
        POSE.scale(-1.0F, -1.0F, 1.0F);
        POSE.translate(0.0F, MASK_Y_OFFSET, 0.0F);
        int brightness = Mth.floor(255.0F * MASK_MAX_BRIGHTNESS
                * RecapTimeline.fraction(MASK_BRIGHTEN_START_MILLIS, RecapTimeline.MASK_END_MILLIS));
        submit(MASK_MODEL, collector, MASK_MODEL.renderType(MaskModel.TEXTURE), argb(brightness));
    }

    private static void submitReplay(Minecraft minecraft, SubmitNodeCollector collector) {
        if (!ReplayRecorder.prepareCurrentFrame(minecraft)) {
            return;
        }
        float progress = RecapTimeline.playbackProgress();
        float distance = Mth.lerp(progress, REPLAY_START_DISTANCE, REPLAY_END_DISTANCE);
        float aspect = ReplayRecorder.replayAspectRatio();
        int brightness = Mth.floor(255.0F * RecapTimeline.fraction(
                RecapTimeline.REPLAY_APPEAR_MILLIS,
                RecapTimeline.PLAYBACK_START_MILLIS
        ));
        POSE.setIdentity();
        POSE.translate(0.0F, 0.0F, -distance);
        POSE.scale(REPLAY_WIDTH, REPLAY_WIDTH / aspect, 1.0F);
        submit(REPLAY_PLANE, collector, RecapRenderTypes.REPLAY, argb(brightness));
    }

    private static void submitStreams(SubmitNodeCollector collector) {
        STREAM_MODEL.setupAnim(RENDER_STATE);
        float z = streamZ();
        for (StreamPlacement stream : STREAMS) {
            POSE.setIdentity();
            POSE.translate(0.0F, 0.0F, z + stream.depth());
            POSE.mulPose(Axis.ZP.rotationDegrees(stream.roll()));
            POSE.scale(STREAM_XY_SCALE, STREAM_XY_SCALE, STREAM_Z_SCALE);
            submit(STREAM_MODEL, collector, RecapRenderTypes.STREAMS, STREAM_TINT);
        }
    }

    private static float streamZ() {
        long elapsed = Math.min(RecapTimeline.elapsedMillis(), RecapTimeline.playbackEndMillis());
        float progress = STREAM_SPEED * Math.max(
                0.0F,
                (elapsed - RecapTimeline.REPLAY_APPEAR_MILLIS) / (float) RecapTimeline.MASK_ANIM_DURATION_MILLIS
        );
        return maskZ(progress);
    }

    private static float maskZ() {
        return maskZ(RecapTimeline.maskProgress());
    }

    private static float maskZ(float progress) {
        return -Mth.lerp(progress, RecapTimeline.MASK_START_DISTANCE, RecapTimeline.MASK_END_DISTANCE);
    }

    private static void submit(
            Model<EntityRenderState> model,
            SubmitNodeCollector collector,
            RenderType renderType,
            int tint
    ) {
        collector.submitModel(model, RENDER_STATE, POSE, renderType, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, tint, null, 0, null);
    }

    private static int argb(int brightness) {
        return 0xFF000000 | brightness << 16 | brightness << 8 | brightness;
    }

    private record StreamPlacement(float roll, float depth) {
    }
}
