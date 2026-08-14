package net.dialingspoon.respawnrecap.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class ReplayPlaneModel extends Model<EntityRenderState> {
    private ReplayPlaneModel(ModelPart root) {
        super(root, RenderTypes::entityTranslucent);
    }

    public static ReplayPlaneModel create() {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        root.addOrReplaceChild(
                "screen",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO
        );
        return new ReplayPlaneModel(LayerDefinition.create(meshDefinition, 1, 1).bakeRoot());
    }
}
