package wtf.gofancy.mc.repurposedlivings.feature.allay.entity.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wtf.gofancy.mc.repurposedlivings.client.network.ClientPacketHandler;

import java.util.function.Supplier;

public record SetItemInHandPacket(int entityId, ItemStack stack) {

    public void encode(final FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        buf.writeItem(this.stack);
    }

    public static SetItemInHandPacket decode(final FriendlyByteBuf buf) {
        final int entityId = buf.readInt();
        final ItemStack stack = buf.readItem();
        return new SetItemInHandPacket(entityId, stack);
    }

    public void processClientPacket(final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleSetItemInHand(this)));
        ctx.get().setPacketHandled(true);
    }
}
