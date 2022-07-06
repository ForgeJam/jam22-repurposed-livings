package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AllayMapItem extends Item {

    public AllayMapItem() {
        super(new Properties().stacksTo(1));
    }
    
    public static CompoundTag createTargetFrom(BlockPos from) {
        CompoundTag tag = new CompoundTag();
        tag.put("from", NbtUtils.writeBlockPos(from));
        return tag;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        CompoundTag tag = stack.getTag();
        if (tag.contains("from")) {
            BlockPos from = NbtUtils.readBlockPos(tag.getCompound("from"));
            tooltipComponents.add(Component.literal("From: " + from).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (tag.contains("to")) {
            BlockPos to = NbtUtils.readBlockPos(tag.getCompound("to"));
            tooltipComponents.add(Component.literal("To: " + to).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
