package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

import java.util.List;

public class AllayMapDraftItem extends Item {

    public AllayMapDraftItem() {
        super(new Item.Properties().stacksTo(1));
    }

    public static ItemStack create(BlockPos pos, Direction side) {
        CompoundTag tag = new CompoundTag();
        tag.put("from", new ItemTarget(pos, side).serializeNbt());
        ItemStack stack = new ItemStack(ModSetup.ALLAY_MAP_DRAFT.get(), 1);
        stack.setTag(tag);
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        if (ModUtil.isContainer(context.getLevel(), pos, side)) {
            ItemStack draft = context.getItemInHand();
            ItemStack stack = AllayMapItem.createFromDraft(draft, pos, side);
            if (!stack.isEmpty()) {
                context.getPlayer().setItemInHand(context.getHand(), stack);
                return InteractionResult.SUCCESS;
            }
        }
        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(ModUtil.getItemTranslation(this, "complete_draft").withStyle(ChatFormatting.AQUA));
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("from")) {
            ItemTarget from = ItemTarget.fromNbt(tag.getCompound("from"));
            tooltipComponents.add(ModUtil.getTargetTranslation("target.from", from).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
