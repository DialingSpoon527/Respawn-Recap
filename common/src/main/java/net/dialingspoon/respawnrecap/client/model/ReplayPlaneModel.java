package net.dialingspoon.respawnrecap.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public final class ReplayPlaneModel extends Model {
    private ReplayPlaneModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
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
