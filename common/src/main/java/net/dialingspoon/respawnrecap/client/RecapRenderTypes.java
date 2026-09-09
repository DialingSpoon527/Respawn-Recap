package net.dialingspoon.respawnrecap.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.dialingspoon.respawnrecap.RespawnRecap;
import net.dialingspoon.respawnrecap.mixin.RenderPipelinesAccessor;
import net.dialingspoon.respawnrecap.mixin.RenderTypeAccessor;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import org.joml.Matrix4f;

final class RecapRenderTypes {
    static final ResourceLocation REPLAY_TEXTURE = RespawnRecap.id("replay/current_frame");

    private static final ResourceLocation STREAM_TEXTURE = RespawnRecap.id("textures/gui/memory_stream.png");
    private static final long STREAM_CYCLE_MILLIS = 12000L;
    private static final DepthTestFunction OVERLAY_DEPTH = DepthTestFunction.LEQUAL_DEPTH_TEST;
    private static final RenderStateShard.TexturingStateShard STREAM_REVEAL = new RenderStateShard.TexturingStateShard(
            "respawn_recap_stream_reveal",
            () -> RenderSystem.setTextureMatrix(
                    new Matrix4f().translation(
                        0.0F,
                        1.0F - Util.getMillis() % STREAM_CYCLE_MILLIS / (float) STREAM_CYCLE_MILLIS,
                        0.0F
                    )
            ),
            RenderSystem::resetTextureMatrix
    );

    static final RenderType REPLAY = texturedType("replay_screen", REPLAY_TEXTURE, null, DepthTestFunction.LEQUAL_DEPTH_TEST);
    static final RenderType STREAMS = texturedType("memory_stream", STREAM_TEXTURE, STREAM_REVEAL, OVERLAY_DEPTH);
    static final RenderType OCCLUDER = untexturedType("mask_occluder", OVERLAY_DEPTH);
    static final RenderPipeline BLINK_GRADIENT = RenderPipelinesAccessor.respawnrecap$register(
            RenderPipeline.builder()
                    .withLocation(RespawnRecap.id("pipeline/blink_gradient"))
                    .withVertexShader(RespawnRecap.id("core/blink_gradient"))
                    .withFragmentShader(RespawnRecap.id("core/blink_gradient"))
                    .withBlend(BlendFunction.TRANSLUCENT)
                    .withCull(false)
                    .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS)
                    .build()
    );
    public static final RenderType BLINK_GRADIENT_TYPE = createType(
            "respawn_recap_blink_gradient",
            RenderType.SMALL_BUFFER_SIZE,
            false,
            BLINK_GRADIENT,
            RenderType.CompositeState.builder().createCompositeState(false)
    );

    private RecapRenderTypes() {
    }

    private static RenderType texturedType(
            String name,
            ResourceLocation texture,
            RenderStateShard.TexturingStateShard transform,
            DepthTestFunction depth
    ) {
        RenderPipeline pipeline = pipeline(name, "recap_surface", true, depth);
        RenderStateShard.TextureStateShard textureShard = new RenderStateShard.TextureStateShard(texture, TriState.FALSE,false);
        RenderType.CompositeState.CompositeStateBuilder setup = RenderType.CompositeState.builder().setTextureState(textureShard);
        if (transform != null) {
            setup.setTexturingState(transform);
        }
        return createType(name, 1536, true, pipeline, setup.createCompositeState(false));
    }

    private static RenderType untexturedType(String name, DepthTestFunction depth) {
        return createType(name, 1536, false, pipeline(name, name, false, depth), RenderType.CompositeState.builder().createCompositeState(false));
    }

    private static RenderPipeline pipeline(String name, String vertexShader, boolean textured, DepthTestFunction depth) {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelinesAccessor.respawnrecap$matricesColorFogSnippet())
                .withLocation(RespawnRecap.id("pipeline/" + name))
                .withVertexShader(RespawnRecap.id("core/" + vertexShader))
                .withFragmentShader(RespawnRecap.id("core/" + name))
                .withBlend(BlendFunction.TRANSLUCENT)
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
                .withDepthTestFunction(depth)
                .withDepthWrite(name.equals("replay_screen"));
        if (textured) {
            builder.withSampler("Sampler0");
        }
        return RenderPipelinesAccessor.respawnrecap$register(builder.build());
    }

    private static RenderType createType(String name, int bufferSize, boolean sort, RenderPipeline pipeline, RenderType.CompositeState setup) {
        return RenderTypeAccessor.respawnrecap$create("respawn_recap_" + name, bufferSize, false, sort, pipeline, setup);
    }
}
