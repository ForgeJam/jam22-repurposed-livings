package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.capabilities.Capabilities;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapData;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
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

            if (stack.getItem() == ModSetup.ALLAY_MAP.get()) {
                final var data = player.level.getCapability(Capabilities.ALLAY_MAP_DATA)
                        .resolve()
                        .orElseThrow()
                        .get(stack)
                        .orElseThrow();

                final var currentTarget = this.target.getter.apply(data);
                final var newTarget = new ItemTarget(currentTarget.pos(), this.side);

                this.target.setter.accept(data, newTarget);
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum Target {
        SOURCE(AllayMapData::getSource, AllayMapData::setSource),
        DESTINATION(AllayMapData::getDestination, AllayMapData::setDestination);

        public final Function<AllayMapData, ItemTarget> getter;
        public final BiConsumer<AllayMapData, ItemTarget> setter;

        Target(Function<AllayMapData, ItemTarget> getter, BiConsumer<AllayMapData, ItemTarget> setter) {
            this.getter = getter;
            this.setter = setter;
        }
    }
}
