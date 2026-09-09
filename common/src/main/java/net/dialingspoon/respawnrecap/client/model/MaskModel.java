package net.dialingspoon.respawnrecap.client.model;

import net.dialingspoon.respawnrecap.RespawnRecap;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class MaskModel extends Model {
	public static final ResourceLocation TEXTURE = RespawnRecap.id("textures/entity/mask.png");

	private MaskModel(ModelPart root) {
		super(root, RenderType::entityCutout);
	}

	public static MaskModel create() {
		return new MaskModel(createBodyLayer().bakeRoot());
	}

	private static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition eyes = root.addOrReplaceChild("eyes", CubeListBuilder.create(), PartPose.offset(0.0354F, 22.8911F, 1.0673F));

		eyes.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 6).addBox(-11.0F, 0.0F, -2.0F, 11.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(24, 29).addBox(-1.0F, 1.0F, -2.0F, 1.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 39).addBox(-11.0F, 1.0F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 11).addBox(-10.0F, 5.0F, -2.0F, 9.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(34, 42).addBox(-6.0F, 6.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(66, 20).addBox(-5.0F, 7.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 53).addBox(-9.0F, 4.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 40).addBox(-10.0F, 2.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 51).addBox(-10.0F, 1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 66).addBox(-7.0F, 1.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 28).addBox(-2.0F, 1.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 49).addBox(-4.0F, 4.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 24).addBox(-5.0F, 2.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 47).addBox(-5.0F, 1.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 45).addBox(-5.0F, 6.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 66).addBox(-2.0F, 6.0F, 0.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(54, 18).addBox(-4.0F, 9.0F, 0.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 21).addBox(-6.0F, 10.0F, -2.0F, 5.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(42, 7).addBox(-6.0F, 1.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(12, 29).addBox(-12.0F, 0.0F, -4.0F, 1.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(52, 12).addBox(-8.0F, -2.0F, -3.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-12.0F, -1.0F, -4.0F, 13.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(24, 18).addBox(-8.0F, -2.0F, -1.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0354F, -5.7393F, 0.1667F, 0.0F, 0.0F, -0.7854F));

		eyes.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 16).addBox(-4.5F, -0.5F, -2.5F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.3286F, 4.1601F, -1.3333F, 0.0F, 0.0F, -0.7854F));

		eyes.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 56).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.9144F, -2.9109F, -2.3333F, 0.0F, 0.0F, -0.7854F));

		eyes.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 22).addBox(-0.5F, -6.0F, -2.5F, 1.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5608F, -1.8503F, -1.3333F, 0.0F, 0.0F, -0.7854F));

		eyes.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(40, 52).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.8638F, -7.1536F, 0.1667F, 0.0F, 0.0F, -0.7854F));

		eyes.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(46, 26).addBox(-0.5F, -4.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.8537F, -3.9716F, 0.1667F, 0.0F, 0.0F, -0.7854F));

		eyes.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(52, 30).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.7931F, -7.1536F, 0.1667F, 0.0F, 0.0F, -0.7854F));

		root.addOrReplaceChild("face", CubeListBuilder.create().texOffs(12, 22).addBox(-1.9748F, 3.0605F, -3.171F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(30, 6).addBox(-0.9748F, -4.9395F, -1.171F, 2.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(44, 42).addBox(1.0252F, -3.9395F, -1.171F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(22, 43).addBox(-1.9748F, -3.9395F, -1.171F, 1.0F, 7.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(10, 41).addBox(2.0253F, 2.0605F, -3.171F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(42, 0).addBox(-2.9747F, 2.0605F, -3.171F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 48).addBox(2.0253F, -2.9395F, -1.171F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(8, 48).addBox(-2.9747F, -2.9395F, -1.171F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(54, 14).addBox(3.0253F, -1.9395F, -1.171F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(54, 0).addBox(-3.9747F, -1.9395F, -1.171F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(34, 26).addBox(3.0253F, -0.9395F, -3.17F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(34, 34).addBox(-3.9747F, -0.9395F, -3.17F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0253F, 30.0913F, 0.404F));

		PartDefinition trhorn = root.addOrReplaceChild("trhorn", CubeListBuilder.create().texOffs(44, 15).addBox(-5.8967F, 2.0704F, -1.1567F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 4).addBox(-4.8967F, 5.0704F, -1.1567F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 34).addBox(-3.8967F, 6.0704F, -1.1567F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(6.8967F, 13.0814F, -1.6094F));

		trhorn.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(16, 68).addBox(11.0F, 2.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 56).addBox(7.0F, 4.0F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 59).addBox(10.0F, 1.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 45).addBox(9.0F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 63).addBox(8.0F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 59).addBox(7.0F, -2.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 57).addBox(6.0F, -3.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(62, 24).addBox(5.0F, -4.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 63).addBox(4.0F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(44, 56).addBox(3.0F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(46, 36).addBox(-1.0F, -5.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(40, 50).addBox(-1.0F, -6.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 11).addBox(-2.0F, -7.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(58, 20).addBox(-2.0F, -1.0F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0717F, -0.4046F, -0.8317F, 0.4899F, -0.1932F, -0.102F));

		trhorn.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(56, 55).addBox(-1.0F, 0.5F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0967F, -0.9296F, -1.0317F, 0.359F, -0.1932F, -0.102F));

		PartDefinition rhorn = root.addOrReplaceChild("rhorn", CubeListBuilder.create(), PartPose.offset(13.8792F, 26.3781F, 0.9146F));

		rhorn.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(62, 55).addBox(6.5F, -2.5F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 65).addBox(8.5F, -1.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(62, 40).addBox(7.5F, -0.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 65).addBox(6.5F, 0.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 63).addBox(5.5F, 1.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 59).addBox(3.5F, 2.5F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 58).addBox(4.5F, 2.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 59).addBox(1.5F, 3.5F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(64, 50).addBox(2.5F, 3.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 34).addBox(-0.5F, 8.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(36, 0).addBox(-0.5F, 3.5F, 0.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 48).addBox(-1.5F, 3.5F, 0.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4939F, -0.1339F, -1.0807F, -0.1237F, -0.2746F, -0.7793F));

		rhorn.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(26, 55).addBox(-0.5F, -0.5F, 0.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4439F, -0.1339F, -1.1807F, -0.0873F, -0.2182F, -0.7854F));

		PartDefinition brhorn = root.addOrReplaceChild("brhorn", CubeListBuilder.create(), PartPose.offset(6.1606F, 36.0646F, 2.2203F));

		brhorn.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(30, 43).addBox(4.5F, -2.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(68, 44).addBox(-4.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(26, 53).addBox(-6.5F, -2.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 67).addBox(-5.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 26).addBox(-4.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 66).addBox(-3.5F, -1.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(28, 60).addBox(-2.5F, -1.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 7).addBox(-0.5F, -1.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 60).addBox(3.5F, -2.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 41).addBox(5.5F, -2.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.3323F, 0.1761F, -0.0985F, -0.1082F, 0.4877F, -0.8056F));

		brhorn.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(38, 56).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.3247F, -4.7707F, -2.4863F, -0.1066F, 0.4574F, -0.8019F));

		PartDefinition tlhorn = root.addOrReplaceChild("tlhorn", CubeListBuilder.create().texOffs(48, 20).addBox(2.8967F, 2.0704F, -1.1567F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(56, 37).addBox(2.8967F, 5.0704F, -1.1567F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(64, 37).addBox(2.8967F, 6.0704F, -1.1567F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.8967F, 13.0814F, -1.6094F));

		tlhorn.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(42, 68).addBox(-12.0F, 2.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 34).addBox(-10.0F, 4.0F, -0.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 62).addBox(-11.0F, 1.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 15).addBox(-10.0F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 10).addBox(-9.0F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(62, 14).addBox(-8.0F, -2.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(62, 6).addBox(-7.0F, -3.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(44, 62).addBox(-6.0F, -4.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 5).addBox(-5.0F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(62, 0).addBox(-4.0F, -5.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 50).addBox(-3.0F, -5.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(68, 53).addBox(0.0F, -6.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 56).addBox(1.0F, -7.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(58, 22).addBox(-1.0F, -1.0F, -0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0717F, -0.4046F, -0.8317F, 0.4899F, 0.1932F, 0.102F));

		tlhorn.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(38, 61).addBox(-1.0F, 0.5F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0967F, -0.9296F, -1.0317F, 0.359F, 0.1932F, 0.102F));

		PartDefinition lhorn = root.addOrReplaceChild("lhorn", CubeListBuilder.create(), PartPose.offset(-13.8792F, 26.3781F, 0.9146F));

		lhorn.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(28, 64).addBox(-8.5F, -2.5F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 0).addBox(-9.5F, -1.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 63).addBox(-8.5F, -0.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 65).addBox(-7.5F, 0.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 65).addBox(-6.5F, 1.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 59).addBox(-4.5F, 2.5F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(38, 65).addBox(-5.5F, 2.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 60).addBox(-2.5F, 3.5F, 0.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 65).addBox(-3.5F, 3.5F, 0.0F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 68).addBox(-0.5F, 8.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 53).addBox(-1.5F, 3.5F, 0.0F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 49).addBox(0.5F, 3.5F, 0.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4939F, -0.1339F, -1.0807F, -0.1237F, 0.2746F, 0.7793F));

		lhorn.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(32, 55).addBox(-1.5F, -0.5F, 0.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4439F, -0.1339F, -1.1807F, -0.0873F, 0.2182F, 0.7854F));

		PartDefinition blhorn = root.addOrReplaceChild("blhorn", CubeListBuilder.create(), PartPose.offset(-6.1606F, 36.0646F, 2.2203F));

		blhorn.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(8, 56).addBox(-5.5F, -2.0F, -0.5F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(68, 50).addBox(3.5F, -3.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 32).addBox(5.5F, -2.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 68).addBox(4.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(68, 47).addBox(3.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 68).addBox(2.5F, -1.0F, -0.5F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(60, 30).addBox(0.5F, -1.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(52, 25).addBox(-3.5F, -1.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(34, 60).addBox(-4.5F, -2.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 52).addBox(-6.5F, -2.0F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3323F, 0.1761F, -0.0985F, -0.1082F, -0.4877F, 0.8056F));

		blhorn.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(56, 40).addBox(-1.0F, -4.0F, -0.5F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.3247F, -4.7707F, -2.4863F, -0.1066F, -0.4574F, 0.8019F));

		return LayerDefinition.create(mesh, 128, 128);
	}
}
