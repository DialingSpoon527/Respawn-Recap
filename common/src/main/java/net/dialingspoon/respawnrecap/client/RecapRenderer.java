package net.dialingspoon.respawnrecap.client;

import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Projection;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.StagedVertexBuffer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;

import java.util.Optional;
import java.util.OptionalDouble;

public final class RecapRenderer implements AutoCloseable {
    private static final Vector4f BLACK = new Vector4f(0.0F, 0.0F, 0.0F, 1.0F);
    private static final float NEAR_PLANE = 0.05F;
    private static final float FAR_PLANE = 100.0F;
    private static final float BLINK_FEATHER_FRACTION = 0.03F;

    private final Projection projection = new Projection();
    private final ProjectionMatrixBuffer projectionBuffer = new ProjectionMatrixBuffer("Respawn Recap projection");
    private final StagedVertexBuffer blinkBuffer = new StagedVertexBuffer(() -> "Respawn Recap blink", 256);

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
                    .clearColorTexture(minecraft.gameRenderer.mainRenderTarget().getColorTexture(), BLACK);
            SubmitNodeStorage nodes = new SubmitNodeStorage();
            RecapScene.submitEffects(minecraft, nodes);
            renderPass(minecraft, dispatcher, nodes);
            nodes = new SubmitNodeStorage();
            RecapScene.submitOccluder(nodes);
            renderPass(minecraft, dispatcher, nodes);
            renderBlink(minecraft, RecapTimeline.blinkProgress());
            nodes = new SubmitNodeStorage();
            RecapScene.submitMask(nodes);
            renderPass(minecraft, dispatcher, nodes);
        } finally {
            modelView.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }

    private void renderBlink(Minecraft minecraft, float progress) {
        if (progress <= 0.0F) {
            return;
        }
        var target = minecraft.gameRenderer.mainRenderTarget();
        var color = target.getColorTexture();
        var depth = target.getDepthTexture();
        int width = color.getWidth(0);
        int height = color.getHeight(0);
        int barHeight = Math.min((height + 1) / 2, Math.round(height * 0.5F * progress));
        if (barHeight <= 0) {
            return;
        }
        var encoder = RenderSystem.getDevice().createCommandEncoder();
        encoder.clearColorAndDepthTextures(color, BLACK, depth, 0.0D, 0, 0, width, barHeight, 0);
        encoder.clearColorAndDepthTextures(color, BLACK, depth, 0.0D, 0, height - barHeight, width, barHeight, 0);

        int openHeight = height - barHeight * 2;
        int featherHeight = Math.min(openHeight / 2, Math.max(1, Math.round(height * BLINK_FEATHER_FRACTION)));
        if (featherHeight <= 0) {
            return;
        }
        float boundary = 1.0F - 2.0F * barHeight / height;
        float feather = 1.0F - 2.0F * (barHeight + featherHeight) / height;
        var draw = this.blinkBuffer.appendDraw(DefaultVertexFormat.POSITION_COLOR, PrimitiveTopology.QUADS);
        VertexConsumer vertices = this.blinkBuffer.getVertexBuilder(draw);
        addGradientQuad(vertices, boundary, feather);
        addGradientQuad(vertices, -boundary, -feather);
        this.blinkBuffer.upload();
        var executeInfo = this.blinkBuffer.getExecuteInfo(draw);
        if (executeInfo != null) {
            try (var renderPass = encoder.createRenderPass(
                    () -> "Respawn Recap blink feather",
                    target.getColorTextureView(),
                    Optional.empty()
            )) {
                renderPass.setPipeline(RenderSystem.getCompiledPipeline(RecapRenderTypes.BLINK_GRADIENT));
                renderPass.setVertexBuffer(0, executeInfo.vertexBuffer().slice());
                renderPass.setIndexBuffer(executeInfo.indexBuffer(), executeInfo.indexType());
                renderPass.drawIndexed(executeInfo.indexCount(), 1, executeInfo.firstIndex(), executeInfo.baseVertex(), 0);
            }
        }
        this.blinkBuffer.endFrame();
    }

    private static void addGradientQuad(VertexConsumer vertices, float boundary, float feather) {
        vertices.addVertex(-1.0F, boundary, 0.0F).setColor(0xFF000000);
        vertices.addVertex(-1.0F, feather, 0.0F).setColor(0x00000000);
        vertices.addVertex(1.0F, feather, 0.0F).setColor(0x00000000);
        vertices.addVertex(1.0F, boundary, 0.0F).setColor(0xFF000000);
    }

    private static void renderPass(Minecraft minecraft, FeatureRenderDispatcher dispatcher, SubmitNodeStorage nodes) {
        var target = minecraft.gameRenderer.mainRenderTarget();
        var encoder = RenderSystem.getDevice().createCommandEncoder();

        encoder.clearDepthTexture(
                target.getDepthTexture(),
                0.0D
        );

        try (
                FeatureRenderDispatcher.PreparedFrame frame =
                        dispatcher.prepareFrame(nodes);

                RenderPass renderPass = encoder.createRenderPass(
                        () -> "Respawn Recap features",
                        target.getColorTextureView(),
                        Optional.empty(),
                        target.getDepthTextureView(),
                        OptionalDouble.empty()
                )
        ) {
            RenderSystem.bindDefaultUniforms(renderPass);
            FeatureRenderDispatcher.renderAllFeatures(renderPass, frame);
        }
    }

    @Override
    public void close() {
        Minecraft minecraft = Minecraft.getInstance();
        RecapController.close(minecraft);
        this.blinkBuffer.close();
        this.projectionBuffer.close();
    }
}
