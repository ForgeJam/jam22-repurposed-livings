package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import wtf.gofancy.mc.repurposedlivings.ModSetup;

import java.util.function.Supplier;

public record UpdateAllayMapTarget(InteractionHand hand, Target target, CompoundTag itemTargetTag) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeEnum(this.target);
        buf.writeNbt(this.itemTargetTag);
    }

    public static UpdateAllayMapTarget decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        Target target = buf.readEnum(Target.class);
        CompoundTag itemTargetTag = buf.readNbt();
        return new UpdateAllayMapTarget(hand, target, itemTargetTag);
    }

    public void processServerPacket(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            ItemStack stack = player.getItemInHand(this.hand);
            CompoundTag tag = stack.getTag();
            if (stack.getItem() == ModSetup.ALLAY_MAP.get() && tag.contains(this.target.nbtName)) {
                tag.put(this.target.nbtName, this.itemTargetTag);
            }
        });
        ctx.get().setPacketHandled(true);
    }
    
    public enum Target {
        SOURCE("from"),
        DESTINATION("to");
        
        public final String nbtName;

        Target(String nbtName) {
            this.nbtName = nbtName;
        }
    }
}
