package wtf.gofancy.mc.repurposedlivings.container;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.capabilities.Capabilities;
import wtf.gofancy.mc.repurposedlivings.network.Network;
import wtf.gofancy.mc.repurposedlivings.network.UpdateAllayMapTargetSide;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class AllayMapContainer extends AbstractContainerMenu {
    private final InteractionHand hand;
    
    private ItemTarget sourceTarget;
    private ItemTarget destinationTarget;

    public AllayMapContainer(int containerId, InteractionHand hand, Player player) {
        super(ModSetup.POWERGEN_CONTAINER.get(), containerId);

        this.hand = hand;
        ItemStack stack = player.getItemInHand(hand);

        final var data = player.level.getCapability(Capabilities.ALLAY_MAP_DATA)
                .resolve()
                .orElseThrow()
                .get(stack)
                .orElseThrow();

        this.sourceTarget = data.getSource().orElseThrow();
        this.destinationTarget = data.getDestination().orElseThrow();
    }

    public ItemTarget getSourceTarget() {
        return this.sourceTarget;
    }

    public void setSourceTarget(ItemTarget target) {
        this.sourceTarget = target;
    }

    public ItemTarget getDestinationTarget() {
        return this.destinationTarget;
    }

    public void setDestinationTarget(ItemTarget target) {
        this.destinationTarget = target;
    }

    public Direction setTargetSide(UpdateAllayMapTargetSide.Target target, Supplier<ItemTarget> getter, Consumer<ItemTarget> setter) {
        ItemTarget itemTarget = getter.get();
        Direction next = Direction.values()[(itemTarget.side().ordinal() + 1) % Direction.values().length];
        setter.accept(itemTarget.withSide(next));
        Network.INSTANCE.sendToServer(new UpdateAllayMapTargetSide(this.hand, target, next));
        return next;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getItemInHand(this.hand).getItem() == ModSetup.ALLAY_MAP.get();
    }
}
