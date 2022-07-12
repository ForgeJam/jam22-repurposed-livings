package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

import java.util.function.Supplier;

public record UpdateAllayMapTargetSide(InteractionHand hand, Target target, Direction side) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.hand);
        buf.writeEnum(this.target);
        buf.writeEnum(this.side);
    }

    public static UpdateAllayMapTargetSide decode(FriendlyByteBuf buf) {
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        Target target = buf.readEnum(Target.class);
        Direction side = buf.readEnum(Direction.class);
        return new UpdateAllayMapTargetSide(hand, target, side);
    }

    public void processServerPacket(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            ItemStack stack = player.getItemInHand(this.hand);
            CompoundTag tag = stack.getTag();
            if (stack.getItem() == ModSetup.ALLAY_MAP.get() && tag.contains(this.target.nbtName)) {
                ItemTarget target = ItemTarget.fromNbt(tag.getCompound(this.target.nbtName));
                ItemTarget newTarget = new ItemTarget(target.pos(), this.side);
                tag.put(this.target.nbtName, newTarget.serializeNbt());
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
