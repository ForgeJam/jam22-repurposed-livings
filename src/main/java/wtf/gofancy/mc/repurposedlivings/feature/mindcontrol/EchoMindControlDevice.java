package wtf.gofancy.mc.repurposedlivings.feature.mindcontrol;

import net.minecraft.world.entity.animal.allay.Allay;

/**
 * A stronger, more effective version of the {@link MindControlDevice}
 * that can be applied to Allays at any time
 */
public class EchoMindControlDevice extends MindControlDevice {

    public EchoMindControlDevice(final Properties properties) {
        super(properties);
    }

    @Override
    public boolean canAttachToAllay(final Allay allay) {
        // Can hijack allays at any time
        return true;
    }
}
