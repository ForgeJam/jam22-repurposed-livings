package wtf.gofancy.mc.repurposedlivings;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wtf.gofancy.mc.repurposedlivings.behavior.GoToTargetPosition;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapItem;

import java.util.Optional;

public class EventHandler {
    
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getWorld();
        BlockPos pos = event.getPos();
        Block block = level.getBlockState(pos).getBlock();
        Player player = event.getPlayer();
        
        if (player.isShiftKeyDown() && block instanceof ChestBlock && event.getItemStack().getItem() instanceof EmptyMapItem) {
            CompoundTag tag = AllayMapItem.createTarget(pos, pos);
            ItemStack stack = new ItemStack(ModSetup.ALLAY_MAP.get());
            stack.setTag(tag);
            
            player.setItemInHand(event.getHand(), stack);
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        
        if (entity instanceof Allay allay) {
            allay.getBrain().getMemories().put(ModSetup.ALLAY_DELIVERY_TARET.get(), Optional.empty());
            
            allay.getBrain().addActivityAndRemoveMemoryWhenStopped(
                ModSetup.ALLAY_PICK_UP_ITEM.get(),
                10,
                ImmutableList.of(new GoToTargetPosition<>(ModSetup.ALLAY_DELIVERY_TARET.get(), 1.75F)),
                ModSetup.ALLAY_DELIVERY_TARET.get()
            );
        }
    }
    
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();
        
        if (target instanceof Allay allay && stack.getItem() instanceof AllayMapItem) {
            BlockPos targetPos = NbtUtils.readBlockPos(stack.getTag().getCompound("from"));
            allay.getBrain().setMemory(ModSetup.ALLAY_DELIVERY_TARET.get(), targetPos.above());
            
            allay.getBrain().setActiveActivityIfPossible(ModSetup.ALLAY_PICK_UP_ITEM.get());
            event.setCanceled(true);
        }
    }
}
