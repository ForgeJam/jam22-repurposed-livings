package wtf.gofancy.mc.repurposedlivings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wtf.gofancy.mc.repurposedlivings.Capabilities;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = RepurposedLivings.MODID)
public final class AllayMapRenderer {

    /**
     * The {@link net.minecraft.client.renderer.ItemInHandRenderer ItemInHandRenderer} is hardcoded to
     * {@link net.minecraft.world.item.Items#FILLED_MAP Items.FILLED_MAP}
     * <p>
     * This event handler adds support for in-hand rendering of Allay maps by calling back into the {@code ItemInHandRenderer}
     * at a later point, effectively skipping the map check.
     */
    @SubscribeEvent
    public static void onRenderHandEvent(final RenderHandEvent event) {
        if (event.getItemStack().is(ModSetup.ALLAY_MAP.get())) {
            renderAllayMapInHand(
                event.getHand() == InteractionHand.MAIN_HAND,
                event.getItemStack(),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight(),
                event.getInterpolatedPitch(),
                event.getEquipProgress(),
                event.getSwingProgress()
            );
            event.setCanceled(true);
        }
    }

    private static void renderAllayMapInHand(final boolean isHoldingInMainHand, final ItemStack stack, final PoseStack matrixStack, final MultiBufferSource buffer, final int combinedLight, final float pitch, final float equippedProgress, final float swingProgress) {
        final Minecraft minecraft = Minecraft.getInstance();
        final boolean twoHanded = isHoldingInMainHand && minecraft.player.getOffhandItem().isEmpty();

        matrixStack.pushPose();

        if (twoHanded) {
            minecraft.gameRenderer.itemInHandRenderer.renderTwoHandedMap(matrixStack, buffer, combinedLight, pitch, equippedProgress, swingProgress);
        } else {
            final HumanoidArm mainArm = minecraft.player.getMainArm();
            final HumanoidArm activeArm = isHoldingInMainHand ? mainArm : mainArm.getOpposite();

            minecraft.gameRenderer.itemInHandRenderer.renderOneHandedMap(matrixStack, buffer, combinedLight, equippedProgress, activeArm, swingProgress, stack);
        }

        matrixStack.popPose();
    }

    /**
     * Normal {@link net.minecraft.client.renderer.entity.ItemFrameRenderer ItemFrameRenderer} crashes for our map,
     * because it recognizes the Allay map as a map but is then hardcoded for Vanilla maps.
     * <p>
     * This event handler replaces the rendering logic with an AllayMap compatible version. It's basically just a copy
     * from what the {@code ItemFrameRenderer} does, but instead of Hardcoding for Vanilla Maps, we hardcode for Allay Maps :)
     */
    @SubscribeEvent
    public static void onRenderItemInFrame(final RenderItemInFrameEvent event) {
        final ItemStack stack = event.getItemStack();
        if (stack.is(ModSetup.ALLAY_MAP.get())) {
            Minecraft.getInstance().level.getCapability(Capabilities.ALLAY_MAP_DATA).resolve()
                .flatMap(data -> data.get(stack))
                .ifPresent(data -> {
                    final PoseStack poseStack = event.getPoseStack();

                    // copied from ItemFrameRenderer#render
                    poseStack.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
                    poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
                    poseStack.translate(-64.0D, -64.0D, 0.0D);
                    poseStack.translate(0.0D, 0.0D, -1.0D);

                    Minecraft.getInstance().gameRenderer.getMapRenderer().render(
                        event.getPoseStack(),
                        event.getMultiBufferSource(),
                        data.getMapId(),
                        data.getCorrespondingMapData(Minecraft.getInstance().level),
                        true,
                        event.getPackedLight()
                    );

                    event.setCanceled(true);
                });
        }
    }
    
    private AllayMapRenderer() {}
}
