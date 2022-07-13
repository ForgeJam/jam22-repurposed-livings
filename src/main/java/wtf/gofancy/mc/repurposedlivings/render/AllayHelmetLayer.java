package wtf.gofancy.mc.repurposedlivings.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Predicate;

public class AllayHelmetLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final EntityModel<T> model;
    private final ResourceLocation texture;
    private final Predicate<T> shouldRender;
    
    public AllayHelmetLayer(RenderLayerParent<T, M> renderer, EntityRendererProvider.Context context, ModelLayerLocation layerLocation, ResourceLocation texture, Predicate<T> shouldRender) {
        super(renderer);
        this.model = new MindControlDeviceModel<>(context.bakeLayer(layerLocation));
        this.texture = texture;
        this.shouldRender = shouldRender;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.shouldRender.test(livingEntity)) {
            poseStack.pushPose();
            this.model.setupAnim(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.armorCutoutNoCull(this.texture), false, false);
            this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.popPose();
        }
    }
}
