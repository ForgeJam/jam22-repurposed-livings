package wtf.gofancy.mc.repurposedlivings.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = RepurposedLivings.MODID)
public class AllayMapRenderer {

    @SubscribeEvent
    public static void onRenderHandEvent(RenderHandEvent event) {
        if (!event.getItemStack().is(ModSetup.ALLAY_MAP.get())) {
            return;
        }

        renderAllayMap(
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

    private static void renderAllayMap(boolean isHoldingInMainHand,
                                       ItemStack stack,
                                       PoseStack matrixStack,
                                       MultiBufferSource buffer,
                                       int combinedLight,
                                       float pitch,
                                       float equippedProgress,
                                       float swingProgress
    ) {
        boolean twoHanded = isHoldingInMainHand && Minecraft.getInstance().player.getOffhandItem().isEmpty();

        matrixStack.pushPose();

        if (twoHanded) {
            Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderTwoHandedMap(
                    matrixStack,
                    buffer,
                    combinedLight,
                    pitch,
                    equippedProgress,
                    swingProgress
            );
        } else {
            var mainArm = Minecraft.getInstance().player.getMainArm();
            var activeArm = isHoldingInMainHand ? mainArm : mainArm.getOpposite();

            Minecraft.getInstance().gameRenderer.itemInHandRenderer.renderOneHandedMap(
                    matrixStack,
                    buffer,
                    combinedLight,
                    equippedProgress,
                    activeArm,
                    swingProgress,
                    stack
            );
        }

        matrixStack.popPose();
    }
}
