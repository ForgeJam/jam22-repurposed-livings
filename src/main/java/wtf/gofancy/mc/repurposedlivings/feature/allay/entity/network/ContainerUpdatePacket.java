package wtf.gofancy.mc.repurposedlivings.feature.allay.entity.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import wtf.gofancy.mc.repurposedlivings.client.network.ClientPacketHandler;

import java.util.List;
import java.util.function.Supplier;

public record ContainerUpdatePacket(int entityId, List<ItemStack> stacks) {
    
    public void encode(final FriendlyByteBuf buf) {
        buf.writeInt(this.entityId);
        final CompoundTag tag = new CompoundTag();
        final ListTag data = new ListTag();
        this.stacks.stream()
            .map(stack -> stack.save(new CompoundTag()))
            .forEach(data::add);
        tag.put("data", data);
        buf.writeNbt(tag);
    }

    public static ContainerUpdatePacket decode(final FriendlyByteBuf buf) {
        final int entityId = buf.readInt();
        final CompoundTag tag = buf.readNbt();
        final ListTag data = tag.getList("data", Tag.TAG_COMPOUND);
        final List<ItemStack> stacks = data.stream()
            .map(nbt -> ItemStack.of((CompoundTag) nbt))
            .toList();
        return new ContainerUpdatePacket(entityId, stacks);
    }

    public void processClientPacket(final Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleContainerUpdate(this)));
        ctx.get().setPacketHandled(true);
    }
}
