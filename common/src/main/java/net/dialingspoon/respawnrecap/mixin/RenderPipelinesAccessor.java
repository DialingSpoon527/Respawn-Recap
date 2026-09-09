package net.dialingspoon.respawnrecap.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("MATRICES_COLOR_FOG_SNIPPET")
    static RenderPipeline.Snippet respawnrecap$matricesColorFogSnippet() {
        throw new AssertionError();
    }

    @Invoker("register")
    static RenderPipeline respawnrecap$register(RenderPipeline pipeline) {
        throw new AssertionError();
    }
}
