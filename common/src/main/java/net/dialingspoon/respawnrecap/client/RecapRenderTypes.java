package net.dialingspoon.respawnrecap.client;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.dialingspoon.respawnrecap.RespawnRecap;
import net.dialingspoon.respawnrecap.mixin.RenderPipelinesAccessor;
import net.dialingspoon.respawnrecap.mixin.RenderTypeAccessor;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import org.joml.Matrix4f;

final class RecapRenderTypes {
    static final Identifier REPLAY_TEXTURE = RespawnRecap.id("replay/current_frame");

    private static final Identifier STREAM_TEXTURE = RespawnRecap.id("textures/gui/memory_stream.png");
    private static final long STREAM_CYCLE_MILLIS = 12000L;
    private static final DepthStencilState OVERLAY_DEPTH = new DepthStencilState(CompareOp.GREATER_THAN_OR_EQUAL, false);
    private static final TextureTransform STREAM_REVEAL = new TextureTransform(
            "respawn_recap_stream_reveal",
            () -> new Matrix4f().translation(
                    0.0F,
                    1.0F - Util.getMillis() % STREAM_CYCLE_MILLIS / (float) STREAM_CYCLE_MILLIS,
                    0.0F
            )
    );

    static final RenderType REPLAY = texturedType("replay_screen", REPLAY_TEXTURE, null, DepthStencilState.DEFAULT);
    static final RenderType STREAMS = texturedType("memory_stream", STREAM_TEXTURE, STREAM_REVEAL, OVERLAY_DEPTH);
    static final RenderType OCCLUDER = untexturedType("mask_occluder", OVERLAY_DEPTH);
    static final RenderPipeline BLINK_GRADIENT = RenderPipelinesAccessor.respawnrecap$register(
            RenderPipeline.builder()
                    .withLocation(RespawnRecap.id("pipeline/blink_gradient"))
                    .withVertexShader(RespawnRecap.id("core/blink_gradient"))
                    .withFragmentShader(RespawnRecap.id("core/blink_gradient"))
                    .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                    .withCull(false)
                    .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
                    .withPrimitiveTopology(PrimitiveTopology.QUADS)
                    .build()
    );

    private RecapRenderTypes() {
    }

    private static RenderType texturedType(
            String name,
            Identifier texture,
            TextureTransform transform,
            DepthStencilState depth
    ) {
        RenderPipeline pipeline = pipeline(name, "recap_surface", true, depth);
        RenderSetup.RenderSetupBuilder setup = RenderSetup.builder(pipeline).withTexture("Sampler0", texture).sortOnUpload();
        if (transform != null) {
            setup.setTextureTransform(transform);
        }
        return createType(name, setup.createRenderSetup());
    }

    private static RenderType untexturedType(String name, DepthStencilState depth) {
        return createType(name, RenderSetup.builder(pipeline(name, name, false, depth)).createRenderSetup());
    }

    private static RenderPipeline pipeline(String name, String vertexShader, boolean textured, DepthStencilState depth) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelinesAccessor.respawnrecap$matricesFogSnippet())
                .withLocation(RespawnRecap.id("pipeline/" + name))
                .withVertexShader(RespawnRecap.id("core/" + vertexShader))
                .withFragmentShader(RespawnRecap.id("core/" + name))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                .withPrimitiveTopology(PrimitiveTopology.QUADS)
                .withDepthStencilState(depth);
        if (textured) {
            builder.withBindGroupLayout(BindGroupLayouts.SAMPLER0);
        }
        return RenderPipelinesAccessor.respawnrecap$register(builder.build());
    }

    private static RenderType createType(String name, RenderSetup setup) {
        return RenderTypeAccessor.respawnrecap$create("respawn_recap_" + name, setup);
    }
}
