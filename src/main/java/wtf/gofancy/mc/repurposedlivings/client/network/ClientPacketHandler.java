package wtf.gofancy.mc.repurposedlivings.client.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import wtf.gofancy.mc.repurposedlivings.Capabilities;
import wtf.gofancy.mc.repurposedlivings.feature.allay.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.feature.allay.entity.network.ContainerUpdatePacket;
import wtf.gofancy.mc.repurposedlivings.feature.allay.entity.network.SetItemInHandPacket;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.AllayMapData;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

public final class ClientPacketHandler {

    public static void handleAllayMapDataUpdate(final AllayMapData data) {
        Minecraft.getInstance().level.getCapability(Capabilities.ALLAY_MAP_DATA)
            .ifPresent(mapData -> mapData.set(data.getMapId(), data));
    }

    public static void handleSetItemInHand(final SetItemInHandPacket packet) {
        final Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living) {
            living.setItemInHand(InteractionHand.MAIN_HAND, packet.stack());
        }
    }

    public static void handleContainerUpdate(final ContainerUpdatePacket packet) {
        final Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if (entity instanceof HijackedAllay allay) {
            ModUtil.updateContainerContent(allay.getExtendedInventory(), packet.stacks());
        }
    }

    private ClientPacketHandler() {}
}
