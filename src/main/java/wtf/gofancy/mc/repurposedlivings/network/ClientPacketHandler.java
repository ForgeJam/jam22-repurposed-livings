package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ClientPacketHandler {
    
    public static void handleSetItemInHandPacket(SetItemInHandPacket packet) {
        Entity entity = Minecraft.getInstance().level.getEntity(packet.entityId());
        if (entity instanceof LivingEntity living) {
            living.setItemInHand(InteractionHand.MAIN_HAND, packet.stack());
        }
    }
    
    private ClientPacketHandler() {}
}
