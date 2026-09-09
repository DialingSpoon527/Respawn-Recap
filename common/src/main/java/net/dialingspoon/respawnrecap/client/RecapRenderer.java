package net.dialingspoon.respawnrecap.client;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.joml.Matrix4fStack;

public final class RecapRenderer implements AutoCloseable {
    private static final int BLACK = 0xFF000000;
    private static final float NEAR_PLANE = 0.05F;
    private static final float FAR_PLANE = 100.0F;
    private static final float BLINK_FEATHER_FRACTION = 0.03F;

    private final Projection projection = new Projection();
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("Respawn Recap projection");

    public void render(Minecraft minecraft, FeatureRenderDispatcher dispatcher) {
        float respawnBlink = RecapController.respawnBlinkProgress();
        boolean recapActive = RecapController.isActive();
        if (!minecraft.isGameLoadFinished() || (!recapActive && respawnBlink <= 0.0F)) {
            return;
        }
        int width = Math.max(1, minecraft.getWindow().getWidth());
        int height = Math.max(1, minecraft.getWindow().getHeight());
        this.projection.setupPerspective(NEAR_PLANE, FAR_PLANE, minecraft.options.fov().get(), width, height);
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(this.projectionBuffer.getBuffer(this.projection), ProjectionType.PERSPECTIVE);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        try {
            if (!recapActive) {
                renderBlink(minecraft, respawnBlink);
                return;
            }
            RenderSystem.getDevice()
                    .createCommandEncoder()
                    .clearColorTexture(minecraft.getMainRenderTarget().getColorTexture(), BLACK);

            SubmitNodeStorage nodes = dispatcher.getSubmitNodeStorage();

            RecapScene.submitEffects(minecraft, nodes);
            renderPass(minecraft, dispatcher);

            RecapScene.submitOccluder(nodes);
            renderPass(minecraft, dispatcher);
            renderBlink(minecraft, RecapTimeline.blinkProgress());

            RecapScene.submitMask(nodes);
            renderPass(minecraft, dispatcher);

            dispatcher.endFrame();
        } finally {
            modelView.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    private void renderBlink(Minecraft minecraft, float progress) {
        if (progress <= 0.0F) {
            return;
        }
        var target = minecraft.getMainRenderTarget();
        var color = target.getColorTexture();
        var depth = target.getDepthTexture();
        int width = color.getWidth(0);
        int height = color.getHeight(0);
        int barHeight = Math.min((height + 1) / 2, Math.round(height * 0.5F * progress));
        if (barHeight <= 0) {
            return;
        }
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.clearColorAndDepthTextures(color, BLACK, depth, 0.0D, 0, 0, width, barHeight);
        encoder.clearColorAndDepthTextures(color, BLACK, depth, 0.0D, 0, height - barHeight, width, barHeight);

        int openHeight = height - barHeight * 2;
        int featherHeight = Math.min(openHeight / 2, Math.max(1, Math.round(height * BLINK_FEATHER_FRACTION)));
        if (featherHeight <= 0) {
            return;
        }
        float boundary = 1.0F - 2.0F * barHeight / height;
        float feather = 1.0F - 2.0F * (barHeight + featherHeight) / height;
        BufferBuilder vertices = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS,
                DefaultVertexFormat.POSITION_COLOR
        );

        addGradientQuad(vertices, boundary, feather);
        addGradientQuad(vertices, -boundary, -feather);

        RecapRenderTypes.BLINK_GRADIENT_TYPE.draw(vertices.buildOrThrow());
    }

    private static void addGradientQuad(VertexConsumer vertices, float boundary, float feather) {
        vertices.addVertex(-1.0F, boundary, 0.0F).setColor(BLACK);
        vertices.addVertex(-1.0F, feather, 0.0F).setColor(0x00000000);
        vertices.addVertex(1.0F, feather, 0.0F).setColor(0x00000000);
        vertices.addVertex(1.0F, boundary, 0.0F).setColor(BLACK);
    }

    private static void renderPass(Minecraft minecraft, FeatureRenderDispatcher dispatcher) {
        RenderSystem.getDevice()
                .createCommandEncoder()
                .clearDepthTexture(minecraft.getMainRenderTarget().getDepthTexture(), 1.0D);
        dispatcher.renderAllFeatures();
        minecraft.renderBuffers().bufferSource().endBatch();
    }

    @Override
    public void close() {
        Minecraft minecraft = Minecraft.getInstance();
        RecapController.close(minecraft);
        this.projectionBuffer.close();
    }
}
