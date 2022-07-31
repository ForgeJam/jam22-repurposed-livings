package wtf.gofancy.mc.repurposedlivings.feature.allay.map.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wtf.gofancy.mc.repurposedlivings.client.network.ClientPacketHandler;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.AllayMapData;

import java.util.function.Supplier;

@SuppressWarnings("deprecation")
public record UpdateAllayMapDataPacket(AllayMapData data) {

    public void encode(final FriendlyByteBuf buf) {
        buf.writeWithCodec(AllayMapData.CODEC, this.data);
    }

    public static UpdateAllayMapDataPacket decode(final FriendlyByteBuf buf) {
        return new UpdateAllayMapDataPacket(buf.readWithCodec(AllayMapData.CODEC));
    }

    public void processClientPacket(final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleAllayMapDataUpdate(this.data)));
        ctx.get().setPacketHandled(true);
    }
}
