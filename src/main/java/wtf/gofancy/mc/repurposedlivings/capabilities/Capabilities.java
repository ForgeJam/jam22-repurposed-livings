package wtf.gofancy.mc.repurposedlivings.capabilities;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class Capabilities {

    public static Capability<AllayMapDataCapability> ALLAY_MAP_DATA =
            CapabilityManager.get(new CapabilityToken<>() {});

    public static Capability<AllayMapDataSyncFlagCapability> ALLAY_MAP_DATA_SYNC_FLAG =
            CapabilityManager.get(new CapabilityToken<>() {});
}
