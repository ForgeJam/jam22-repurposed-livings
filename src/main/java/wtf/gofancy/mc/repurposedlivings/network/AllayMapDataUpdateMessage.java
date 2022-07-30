package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapData;

import java.util.function.Supplier;

public record AllayMapDataUpdateMessage(int mapId, AllayMapData data) {

    public void encode(final FriendlyByteBuf buf) {
        final CompoundTag nbt = new CompoundTag();
        nbt.put("data", AllayMapData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(false, str -> {}));

        buf.writeInt(mapId);
        buf.writeNbt(nbt);
    }

    public static AllayMapDataUpdateMessage decode(final FriendlyByteBuf buf) {
        final int mapId = buf.readInt();
        final CompoundTag nbt = buf.readNbt();

        return new AllayMapDataUpdateMessage(
                mapId,
                AllayMapData.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow(false, str -> {})
        );
    }

    public void processClientbound(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(
                    Dist.CLIENT,
                    () -> () -> ClientMessageHandler.handleAllayMapDataUpdate(this.mapId, this.data)
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
