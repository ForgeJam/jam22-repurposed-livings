package wtf.gofancy.mc.repurposedlivings;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import wtf.gofancy.mc.repurposedlivings.screen.AllayMapScreen;
import wtf.gofancy.mc.repurposedlivings.render.HijackedAllayRenderer;
import wtf.gofancy.mc.repurposedlivings.render.MindControlDeviceModel;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModClientSetup {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModSetup.POWERGEN_CONTAINER.get(), AllayMapScreen::new));
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MindControlDeviceModel.LAYER_LOCATION, MindControlDeviceModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModSetup.HIJACKED_ALLAY_ENTITY.get(), HijackedAllayRenderer::new);
    }
}
