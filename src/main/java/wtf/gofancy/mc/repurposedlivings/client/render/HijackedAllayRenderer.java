package wtf.gofancy.mc.repurposedlivings.client.render;

import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.item.Item;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;
import wtf.gofancy.mc.repurposedlivings.features.allay.entity.AllayEquipment;
import wtf.gofancy.mc.repurposedlivings.features.allay.entity.HijackedAllay;

public class HijackedAllayRenderer extends AllayRenderer {
    private static final ResourceLocation MIND_CONTROL_DEVICE_TEXTURE = RepurposedLivings.rl( "textures/entity/mind_control_device.png");
    private static final ResourceLocation ECHO_MIND_CONTROL_DEVICE_TEXTURE = RepurposedLivings.rl("textures/entity/echo_mind_control_device.png");

    public HijackedAllayRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new AllayHelmetLayer<>(this, context, MindControlDeviceModel.LAYER_LOCATION, MIND_CONTROL_DEVICE_TEXTURE, allay -> hasMindControlDevice(allay, ModSetup.MIND_CONTROL_DEVICE.get())));
        addLayer(new AllayHelmetLayer<>(this, context, MindControlDeviceModel.LAYER_LOCATION, ECHO_MIND_CONTROL_DEVICE_TEXTURE, allay -> hasMindControlDevice(allay, ModSetup.ECHO_MIND_CONTROL_DEVICE.get())));
    }
    
    public boolean hasMindControlDevice(Allay allay, Item item) {
        return ((HijackedAllay) allay).getItemInSlot(AllayEquipment.CONTROLLER).getItem() == item;
    }
}
