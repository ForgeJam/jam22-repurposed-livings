package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.container.AllayMapContainer;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

import java.util.List;
import java.util.stream.Stream;

/**
 * Contains information about the item source and delivery targets.
 * Can be given to Hijacked Allays.
 */
public class AllayMapItem extends Item {

    public AllayMapItem() {
        super(new Properties().stacksTo(1));
    }

    /**
     * Complete an Allay Map draft by adding a destination target to it.
     * 
     * @param draft the Allay Map draft
     * @param destPos the destination target position
     * @param destSide the destination target side
     * @return the completed Allay Map
     */
    public static ItemStack createFromDraft(ItemStack draft, BlockPos destPos, Direction destSide) {
        BlockPos fromPos = ItemTarget.fromNbt(draft.getTag().getCompound("from")).getRelativePos();
        if (destPos.relative(destSide).closerThan(fromPos, 1)) return ItemStack.EMPTY;

        CompoundTag tag = draft.getTag();
        tag.put("to", new ItemTarget(destPos, destSide).serializeNbt());
        ItemStack stack = new ItemStack(ModSetup.ALLAY_MAP.get(), 1);
        stack.setTag(tag);
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (player instanceof ServerPlayer serverPlayer && ensureTargetsLoaded(level, player, player.getItemInHand(usedHand))) {
            NetworkHooks.openScreen(serverPlayer, new AllayMapMenuProvider(usedHand), buf -> buf.writeEnum(usedHand));
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("from") && tag.contains("to")) {
            ItemTarget from = ItemTarget.fromNbt(tag.getCompound("from"));
            tooltipComponents.add(ModUtil.getTargetTranslation("target.from", from).withStyle(ChatFormatting.DARK_GRAY));

            ItemTarget to = ItemTarget.fromNbt(tag.getCompound("to"));
            tooltipComponents.add(ModUtil.getTargetTranslation("target.to", to).withStyle(ChatFormatting.DARK_GRAY));
        }
        else {
            tooltipComponents.add(ModUtil.getItemTranslation(this, "invalid").withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private boolean ensureTargetsLoaded(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("from") && tag.contains("to")) {
            ItemTarget from = ItemTarget.fromNbt(tag.getCompound("from"));
            ItemTarget to = ItemTarget.fromNbt(tag.getCompound("to"));
            
            boolean loaded = Stream.of(from.pos(), from.getRelativePos(), to.pos(), to.getRelativePos()).allMatch(level::isLoaded);
            if (!loaded) player.displayClientMessage(ModUtil.getItemTranslation(this, "out_of_range").withStyle(ChatFormatting.RED), true);
            else return true;
        }
        return false;
    }
    
    private record AllayMapMenuProvider(InteractionHand hand) implements MenuProvider {

        @Override
            public Component getDisplayName() {
                return ModSetup.ALLAY_MAP.get().getDescription();
            }
    
            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new AllayMapContainer(containerId, this.hand, player);
            }
        }
}
