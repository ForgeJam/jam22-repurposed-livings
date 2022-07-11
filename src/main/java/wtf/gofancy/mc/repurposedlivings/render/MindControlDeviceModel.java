package wtf.gofancy.mc.repurposedlivings.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;

public class MindControlDeviceModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(RepurposedLivings.MODID, "mind_control_device_model"), "main");
    
	private final ModelPart root;

	public MindControlDeviceModel(ModelPart root) {
		this.root = root.getChild("root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partDefinition = meshdefinition.getRoot();
		PartDefinition root = partDefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 0.0F));
        
        root.addOrReplaceChild("helmet", CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.5F, -10.0F, -3.5F, 7.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 4.0F, 0.0F)
        );
        root.addOrReplaceChild("controller", CubeListBuilder.create()
            .texOffs(0, 12)
            .addBox(-1.5F, -11.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
            PartPose.offset(0.0F, 4.0F, 0.0F)
        );

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.getAllParts().forEach(ModelPart::resetPose);
		this.root.xRot = headPitch * ((float)Math.PI / 180F);
		this.root.yRot = netHeadYaw * ((float)Math.PI / 180F);
		
		float swing = Math.min(limbSwingAmount / 0.3F, 1.0F);
		float swingAmount = 1.0F - swing;
		float animAge = ageInTicks * 9.0F * ((float) Math.PI / 180F);
		this.root.y += (float) Math.cos(animAge) * 0.25F * swingAmount;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}
