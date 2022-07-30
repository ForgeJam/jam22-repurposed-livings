package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapData;

import java.util.function.Supplier;

public record UpdateAllayMapDataPacket(AllayMapData data) {

    public void encode(final FriendlyByteBuf buf) {
        final var nbt = new CompoundTag();
        nbt.put("data", AllayMapData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow(false, str -> {}));
        buf.writeNbt(nbt);
    }

    public static UpdateAllayMapDataPacket decode(final FriendlyByteBuf buf) {
        final var nbt = buf.readNbt().get("data");
        final var data = AllayMapData.CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow(false, str -> {});
        return new UpdateAllayMapDataPacket(data);
    }

    public void processClientPacket(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(
                        Dist.CLIENT,
                        () -> () -> ClientPacketHandler.handleAllayMapDataUpdate(this.data)
                ));
        ctx.get().setPacketHandled(true);
    }
}
