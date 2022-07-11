package wtf.gofancy.mc.repurposedlivings.container;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

public class AllayMapContainer extends AbstractContainerMenu {
    private final InteractionHand hand;
    private final ItemTarget sourceTarget;
    private final ItemTarget destinationTarget;

    public AllayMapContainer(int containerId, InteractionHand hand, Player player) {
        super(ModSetup.POWERGEN_CONTAINER.get(), containerId);
        
        this.hand = hand;
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getTag();
        this.sourceTarget = ItemTarget.fromNbt(tag.getCompound("from"));
        this.destinationTarget = ItemTarget.fromNbt(tag.getCompound("to"));
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public ItemTarget getSourceTarget() {
        return this.sourceTarget;
    }

    public ItemTarget getDestinationTarget() {
        return this.destinationTarget;
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
