package net.dialingspoon.respawnrecap.client.model;

import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;

import java.util.Random;

public final class MemoryStreamModel extends Model<EntityRenderState> {
    private static final long ROTATION_DURATION_MILLIS = 16000L;
    private static final long RANDOM_SEED = 0x5245535041574EL;
    private static final float[][] PART_POSITIONS = {
            {32.0F, 272.0F, 1328.0F},
            {-136.0F, -272.0F, -1184.0F}, {-32.0F, -256.0F, 208.0F}, {80.0F, -248.0F, 24.0F},
            {200.0F, -240.0F, -488.0F}, {272.0F, -184.0F, 336.0F}, {216.0F, -192.0F, -800.0F},
            {232.0F, -128.0F, 640.0F}, {288.0F, -80.0F, 1856.0F}, {200.0F, 192.0F, -552.0F},
            {272.0F, 120.0F, -1688.0F}, {192.0F, 16.0F, -1856.0F}, {152.0F, 88.0F, -1616.0F},
            {-16.0F, 80.0F, -2008.0F}, {48.0F, 40.0F, 912.0F}, {104.0F, -136.0F, -1656.0F},
            {-16.0F, -168.0F, -1824.0F}, {-72.0F, -184.0F, -1032.0F}, {-80.0F, -128.0F, 560.0F},
            {-288.0F, -104.0F, -1440.0F}, {-184.0F, -136.0F, -2168.0F}, {-184.0F, 8.0F, 424.0F},
            {-224.0F, 104.0F, 168.0F}, {-168.0F, 80.0F, -216.0F}, {-88.0F, 120.0F, -2368.0F}
    };

    private final ModelPart[] streamParts = new ModelPart[PART_POSITIONS.length];
    private final float[] startingRolls = new float[PART_POSITIONS.length];

    private MemoryStreamModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
        ModelPart bbMain = root.getChild("bb_main");
        Random random = new Random(RANDOM_SEED);
        for (int i = 0; i < this.streamParts.length; i++) {
            this.streamParts[i] = bbMain.getChild("cube_r" + i);
            this.startingRolls[i] = random.nextFloat() * Mth.TWO_PI;
        }
    }

    @Override
    public void setupAnim(EntityRenderState state) {
        super.setupAnim(state);
        float angle = (Util.getMillis() % ROTATION_DURATION_MILLIS)
                / (float) ROTATION_DURATION_MILLIS * Mth.TWO_PI;
        for (int i = 0; i < this.streamParts.length; i++) {
            this.streamParts[i].setRotation(0.0F, 0.0F, this.startingRolls[i] + angle);
        }
    }

    public static MemoryStreamModel create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition streamRoot = root.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        for (int i = 0; i < PART_POSITIONS.length; i++) {
            float[] position = PART_POSITIONS[i];
            addStreamPlane(streamRoot, "cube_r" + i, position[0], position[1], position[2]);
        }

        return new MemoryStreamModel(LayerDefinition.create(mesh, 128, 4096).bakeRoot());
    }

    private static void addStreamPlane(PartDefinition parent, String name, float x, float y, float z) {
        parent.addOrReplaceChild(
                name,
                CubeListBuilder.create().texOffs(-4096, 0).addBox(-32.0F, -32.0F, -2048.0F, 64.0F, 0.0F, 4096.0F, new CubeDeformation(0.0F)),
                PartPose.offset(x, y, z)
        );
    }
}
