package wtf.gofancy.mc.repurposedlivings.render;

import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class HijackedAllayRenderer extends AllayRenderer {

    public HijackedAllayRenderer(EntityRendererProvider.Context context) {
        super(context);
        addLayer(new MindControlDeviceRenderLayer<>(this, context));
    }
}
