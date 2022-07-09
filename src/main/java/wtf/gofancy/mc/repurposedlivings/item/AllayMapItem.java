package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;

import java.util.List;

public class AllayMapItem extends Item {

    public AllayMapItem() {
        super(new Properties().stacksTo(1));
    }
    
    public static ItemStack createFromDraft(ItemStack draft, BlockPos destPos, Direction destSide) {
        CompoundTag tag = draft.getTag();
        tag.put("to", ModUtil.createTargetTag(destPos, destSide));
        ItemStack stack = new ItemStack(ModSetup.ALLAY_MAP.get(), 1);
        stack.setTag(tag);
        return stack;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof HijackedAllay allay) {
            Brain<Allay> brain = allay.getBrain();
            CompoundTag tag = stack.getTag();
            // TODO Find free space around target
            CompoundTag from = tag.getCompound("from");
            BlockPos fromPos = NbtUtils.readBlockPos(from.getCompound("pos"));
            Direction fromSide = Direction.from3DDataValue(from.getInt("side"));
            brain.setMemory(ModSetup.ALLAY_SOURCE_TARET.get(), fromPos.relative(fromSide));
            
            CompoundTag to = tag.getCompound("to");
            BlockPos toPos = NbtUtils.readBlockPos(to.getCompound("pos"));
            Direction toSide = Direction.from3DDataValue(to.getInt("side"));
            brain.setMemory(ModSetup.ALLAY_DELIVERY_TARET.get(), toPos.relative(toSide));
            
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.contains("from") && tag.contains("to")) {
            CompoundTag from = tag.getCompound("from");
            BlockPos fromPos = NbtUtils.readBlockPos(from.getCompound("pos"));
            Direction fromSide = Direction.from3DDataValue(from.getInt("side"));
            tooltipComponents.add(Component.literal("From: " + fromPos + ", " + fromSide).withStyle(ChatFormatting.DARK_GRAY));
            
            CompoundTag to = tag.getCompound("to");
            BlockPos toPos = NbtUtils.readBlockPos(to.getCompound("pos"));
            Direction toSide = Direction.from3DDataValue(to.getInt("side"));
            tooltipComponents.add(Component.literal("To: " + toPos + ", " + toSide).withStyle(ChatFormatting.DARK_GRAY));
        }
        else tooltipComponents.add(Component.literal("Invalid").withStyle(ChatFormatting.DARK_GRAY));
    }
}
