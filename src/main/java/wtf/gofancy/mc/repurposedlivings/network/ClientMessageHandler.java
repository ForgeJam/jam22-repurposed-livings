package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.client.Minecraft;
import wtf.gofancy.mc.repurposedlivings.capabilities.Capabilities;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapData;

public class ClientMessageHandler {

    public static void handleAllayMapDataUpdate(final int mapId, final AllayMapData data) {
        Minecraft.getInstance().level.getCapability(Capabilities.ALLAY_MAP_DATA).resolve().orElseThrow().set(mapId, data);
    }
}
