package net.dialingspoon.respawnrecap.client;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix4f;

public final class RecapRenderer implements AutoCloseable {
    private static final int BLACK = 0xFF000000;
    private static final float NEAR_PLANE = 0.05F;
    private static final float FAR_PLANE = 100.0F;
    private static final float BLINK_FEATHER_FRACTION = 0.03F;

    public void render(Minecraft minecraft) {
        float respawnBlink = RecapController.respawnBlinkProgress();
        boolean recapActive = RecapController.isActive();
        if (!minecraft.isGameLoadFinished() || (!recapActive && respawnBlink <= 0.0F)) {
            return;
        }
        int width = Math.max(1, minecraft.getWindow().getWidth());
        int height = Math.max(1, minecraft.getWindow().getHeight());
        RenderSystem.backupProjectionMatrix();

        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(minecraft.options.fov().get()),
                width / (float) height,
                NEAR_PLANE,
                FAR_PLANE
        );
        RenderSystem.setProjectionMatrix(
                projection,
                VertexSorting.DISTANCE_TO_ORIGIN
        );

        PoseStack modelView = RenderSystem.getModelViewStack();
        modelView.pushPose();
        modelView.setIdentity();
        RenderSystem.applyModelViewMatrix();
        try {
            if (!recapActive) {
                renderBlink(minecraft, respawnBlink);
                return;
            }
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.clear(GlConst.GL_COLOR_BUFFER_BIT, false);

            MultiBufferSource buffers = minecraft.renderBuffers().bufferSource();

            RecapScene.submitEffects(minecraft, buffers);
            renderPass(minecraft);

            RecapScene.submitOccluder(buffers);
            renderPass(minecraft);
            renderBlink(minecraft, RecapTimeline.blinkProgress());

            RecapScene.submitMask(buffers);
            renderPass(minecraft);
        } finally {
            modelView.popPose();
            RenderSystem.applyModelViewMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    private void renderBlink(Minecraft minecraft, float progress) {
        if (progress <= 0.0F) {
            return;
        }
        var target = minecraft.getMainRenderTarget();
        int height = target.height;
        int barHeight = Math.min((height + 1) / 2, Math.round(height * 0.5F * progress));
        if (barHeight <= 0) {
            return;
        }

        int openHeight = height - barHeight * 2;
        int featherHeight = Math.min(openHeight / 2, Math.max(1, Math.round(height * BLINK_FEATHER_FRACTION)));
        float boundary = 1.0F - 2.0F * barHeight / height;
        BufferBuilder vertices = Tesselator.getInstance().getBuilder();
        vertices.begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );

        if (featherHeight > 0) {
            float feather = 1.0F - 2.0F * (barHeight + featherHeight) / height;
            addGradientQuad(vertices, boundary, feather);
            addGradientQuad(vertices, -boundary, -feather);
        }

        addSolidQuad(vertices, boundary, 1.0F);
        addSolidQuad(vertices, -1.0F, -boundary);

        RecapRenderTypes.BLINK_GRADIENT_TYPE.end(vertices, VertexSorting.DISTANCE_TO_ORIGIN);
    }

    private static void addGradientQuad(VertexConsumer vertices, float boundary, float feather) {
        vertices.vertex(-1.0F, boundary, 0.0F).color(BLACK).endVertex();
        vertices.vertex(-1.0F, feather, 0.0F).color(0x00000000).endVertex();
        vertices.vertex(1.0F, feather, 0.0F).color(0x00000000).endVertex();
        vertices.vertex(1.0F, boundary, 0.0F).color(BLACK).endVertex();
    }

    private static void addSolidQuad(VertexConsumer vertices, float y1, float y2) {
        vertices.vertex(-1.0F, y1, 0.0F).color(BLACK).endVertex();
        vertices.vertex( 1.0F, y1, 0.0F).color(BLACK).endVertex();
        vertices.vertex( 1.0F, y2, 0.0F).color(BLACK).endVertex();
        vertices.vertex(-1.0F, y2, 0.0F).color(BLACK).endVertex();
    }

    private static void renderPass(Minecraft minecraft) {
        RenderSystem.clearDepth(1.0D);
        RenderSystem.clear(GlConst.GL_DEPTH_BUFFER_BIT, false);
        minecraft.renderBuffers().bufferSource().endBatch();
    }

    @Override
    public void close() {
        Minecraft minecraft = Minecraft.getInstance();
        RecapController.close(minecraft);
    }
}
