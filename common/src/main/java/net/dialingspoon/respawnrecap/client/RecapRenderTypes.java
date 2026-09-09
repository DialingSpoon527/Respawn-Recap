package net.dialingspoon.respawnrecap.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.dialingspoon.respawnrecap.RespawnRecap;
import net.dialingspoon.respawnrecap.mixin.RenderTypeAccessor;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class RecapRenderTypes {
    static final ResourceLocation REPLAY_TEXTURE = RespawnRecap.id("replay/current_frame");

    private static final ResourceLocation STREAM_TEXTURE = RespawnRecap.id("textures/gui/memory_stream.png");
    private static final long STREAM_CYCLE_MILLIS = 12000L;
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

    static ShaderInstance REPLAY_SHADER;
    static ShaderInstance STREAM_SHADER;
    static ShaderInstance MASK_OCCLUDER_SHADER;
    static ShaderInstance BLINK_GRADIENT_SHADER;

    public static void registerShaders(ShaderRegistrar registrar) throws IOException {
        registrar.register(RespawnRecap.id("replay_screen"), DefaultVertexFormat.NEW_ENTITY, shader -> REPLAY_SHADER = shader);
        registrar.register(RespawnRecap.id("memory_stream"), DefaultVertexFormat.NEW_ENTITY, shader -> STREAM_SHADER = shader);
        registrar.register(RespawnRecap.id("mask_occluder"), DefaultVertexFormat.NEW_ENTITY, shader -> MASK_OCCLUDER_SHADER = shader);
        registrar.register(RespawnRecap.id("blink_gradient"), DefaultVertexFormat.POSITION_COLOR, shader -> BLINK_GRADIENT_SHADER = shader);
    }
    @FunctionalInterface
    public interface ShaderRegistrar {
        void register(ResourceLocation id, VertexFormat format, Consumer<ShaderInstance> onLoaded) throws IOException;
    }

    static final RenderType REPLAY = texturedType("replay_screen", REPLAY_TEXTURE, null, () -> REPLAY_SHADER, true);
    static final RenderType STREAMS = texturedType("memory_stream", STREAM_TEXTURE, STREAM_REVEAL, () -> STREAM_SHADER, false);
    static final RenderType OCCLUDER = untexturedType("mask_occluder", () -> MASK_OCCLUDER_SHADER, false);
    static final RenderType.CompositeState BLINK_GRADIENT = RenderType.CompositeState.builder()
            .setShaderState(new RenderStateShard.ShaderStateShard(() -> BLINK_GRADIENT_SHADER))
            .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
            .setCullState(RenderStateShard.NO_CULL)
            .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
            .setWriteMaskState(RenderStateShard.COLOR_WRITE)
            .createCompositeState(false);

    static final RenderType BLINK_GRADIENT_TYPE = createType(
            "respawn_recap_blink_gradient",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            RenderType.SMALL_BUFFER_SIZE,
            false,
            BLINK_GRADIENT
    );

    private RecapRenderTypes() {
    }

    private static RenderType texturedType(
            String name,
            ResourceLocation texture,
            RenderStateShard.TexturingStateShard transform,
            Supplier<ShaderInstance> shader,
            boolean depthWrite
    ) {
        RenderType.CompositeState state =
                compositeState(shader, new RenderStateShard.TextureStateShard(texture, false, false), transform, depthWrite);
        return createType(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, state);
    }

    private static RenderType untexturedType(String name, Supplier<ShaderInstance> shader, boolean depthWrite) {
        return createType(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, false, compositeState(shader, null, null, depthWrite));
    }

    private static RenderType.CompositeState compositeState(Supplier<ShaderInstance> shader, RenderStateShard.TextureStateShard texture, RenderStateShard.TexturingStateShard transform, boolean depthWrite) {
        RenderType.CompositeState.CompositeStateBuilder state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(shader))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(RenderStateShard.NO_CULL)
                .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                .setWriteMaskState(depthWrite ? RenderStateShard.COLOR_DEPTH_WRITE : RenderStateShard.COLOR_WRITE);
        if (texture != null) {
            state.setTextureState(texture);
        }
        if (transform != null) {
            state.setTexturingState(transform);
        }
        return state.createCompositeState(false);
    }

    private static RenderType createType(String name, VertexFormat vertexFormat, VertexFormat.Mode mode, int bufferSize, boolean sort, RenderType.CompositeState state) {
        return RenderTypeAccessor.respawnrecap$create("respawn_recap_" + name, vertexFormat, mode, bufferSize, false, sort, state);
    }
}
