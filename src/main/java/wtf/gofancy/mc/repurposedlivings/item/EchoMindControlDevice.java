package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.world.entity.animal.allay.Allay;

public class EchoMindControlDevice extends MindControlDevice {

    public EchoMindControlDevice(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canAttachToAllay(Allay allay) {
        return true;
    }
}
