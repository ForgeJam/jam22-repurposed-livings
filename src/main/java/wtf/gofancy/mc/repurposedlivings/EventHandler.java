package wtf.gofancy.mc.repurposedlivings;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapItem;
import wtf.gofancy.mc.repurposedlivings.item.MindControlDeviceItem;

public class EventHandler {
    
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getWorld();
        BlockPos pos = event.getPos();
        Block block = level.getBlockState(pos).getBlock();
        Player player = event.getPlayer();
        
        if (!player.level.isClientSide && player.isShiftKeyDown() && block instanceof ChestBlock) { // TODO Remove chest hardcoding
            ItemStack stack = event.getItemStack();
            Item item = stack.getItem();
            
            if (item instanceof EmptyMapItem) {
                CompoundTag tag = AllayMapItem.createTargetFrom(pos);
                ItemStack allayStack = new ItemStack(ModSetup.ALLAY_MAP.get());
                allayStack.setTag(tag);

                player.setItemInHand(event.getHand(), allayStack);
                event.setCancellationResult(InteractionResult.CONSUME);
                event.setCanceled(true);
            }
            else if (item instanceof AllayMapItem) {
                CompoundTag tag = stack.getTag();
                
                if (tag.contains("from") && !tag.contains("to")) {
                    tag.put("to", NbtUtils.writeBlockPos(pos));
                    
                    event.setCancellationResult(InteractionResult.SUCCESS);
                    event.setCanceled(true);
                }
            }
        }
    }
    
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();
        
        if (target instanceof Allay allay && stack.getItem() instanceof MindControlDeviceItem) { // TODO Sound
            for (InteractionHand hand : InteractionHand.values()) {
                ItemStack stackInHand = allay.getItemInHand(hand);
                ItemEntity item = new ItemEntity(allay.level, allay.getX(), allay.getY(), allay.getZ(), stackInHand);
                item.setDeltaMovement(0, 0, 0);
                allay.level.addFreshEntity(item);
                
                allay.setItemInHand(hand, ItemStack.EMPTY);
            }
            
            CompoundTag allayTag = allay.saveWithoutId(new CompoundTag());
            HijackedAllay hijackedAllay = new HijackedAllay(ModSetup.HIJACKED_ALLAY_ENTITY.get(), allay.level);
            hijackedAllay.load(allayTag);
            
            allay.remove(Entity.RemovalReason.DISCARDED);
            allay.level.addFreshEntity(hijackedAllay);
//            stack.shrink(1);
            
            event.setCancellationResult(InteractionResult.CONSUME);
            event.setCanceled(true);
        }
    }
}
