package net.dialingspoon.respawnrecap.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderType.class)
public interface RenderTypeAccessor {
    @Invoker("create")
    static RenderType.CompositeRenderType respawnrecap$create(String string, int i, boolean bl, boolean bl2, RenderPipeline renderPipeline, RenderType.CompositeState compositeState) {
        throw new AssertionError();
    }
}
