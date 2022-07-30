package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import wtf.gofancy.mc.repurposedlivings.capabilities.Capabilities;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapData;

public final class ClientPacketHandler {

    public static void handleAllayMapDataUpdate(final AllayMapData data) {
        Minecraft.getInstance().level.getCapability(Capabilities.ALLAY_MAP_DATA)
                .resolve()
                .orElseThrow()
                .set(data.getMapId(), data);
    }

    public static void handleSetItemInHandPacket(SetItemInHandPacket packet) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living) {
            living.setItemInHand(InteractionHand.MAIN_HAND, packet.stack());
        }
    }

    private ClientPacketHandler() {}
}
