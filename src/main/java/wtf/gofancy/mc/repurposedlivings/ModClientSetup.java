package wtf.gofancy.mc.repurposedlivings;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import wtf.gofancy.mc.repurposedlivings.render.HijackedAllayRenderer;
import wtf.gofancy.mc.repurposedlivings.render.MindControlDeviceModel;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModClientSetup {
    
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MindControlDeviceModel.LAYER_LOCATION, MindControlDeviceModel::createBodyLayer);
    }
    
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModSetup.HIJACKED_ALLAY_ENTITY.get(), HijackedAllayRenderer::new);
    }
}
