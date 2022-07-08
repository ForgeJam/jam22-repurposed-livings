package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        if (interactionTarget instanceof HijackedAllay allay) {
            Brain<Allay> brain = allay.getBrain();
            CompoundTag tag = stack.getTag();
            // TODO Find free space around target
            BlockPos fromPos = NbtUtils.readBlockPos(tag.getCompound("from"));
            brain.setMemory(ModSetup.ALLAY_SOURCE_TARET.get(), fromPos.above());
            
            BlockPos toPos = NbtUtils.readBlockPos(tag.getCompound("to"));
            brain.setMemory(ModSetup.ALLAY_DELIVERY_TARET.get(), toPos.above());
            
            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, interactionTarget, usedHand);
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
