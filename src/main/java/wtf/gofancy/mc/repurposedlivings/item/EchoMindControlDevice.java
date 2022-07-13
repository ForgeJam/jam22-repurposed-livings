package wtf.gofancy.mc.repurposedlivings.item;

import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;

public class EchoMindControlDevice extends MindControlDevice {

    public EchoMindControlDevice(Properties properties) {
        super(properties);
    }

    @Override
    public void attachToAllay(HijackedAllay allay) {
        allay.onHijackActivated();
    }
}
