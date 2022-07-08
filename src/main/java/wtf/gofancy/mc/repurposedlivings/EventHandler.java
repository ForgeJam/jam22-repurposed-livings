package wtf.gofancy.mc.repurposedlivings;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.animal.allay.Allay;
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
        
        if (!player.level.isClientSide && player.isShiftKeyDown() && block instanceof ChestBlock) {
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
        
        if (target instanceof Allay allay) {
            Item item = stack.getItem();
            if (item instanceof MindControlDeviceItem) {
                CompoundTag allayTag = allay.saveWithoutId(new CompoundTag());
                allay.remove(Entity.RemovalReason.DISCARDED);
                HijackedAllay hijackedAllay = new HijackedAllay(ModSetup.HIJACKED_ALLAY_ENTITY.get(), allay.level);
                hijackedAllay.load(allayTag);
                allay.level.addFreshEntity(hijackedAllay);
                
                stack.shrink(1);
                event.setCancellationResult(InteractionResult.CONSUME);
                event.setCanceled(true);
            }
            else if (item instanceof AllayMapItem) {
                Brain<Allay> brain = allay.getBrain();
                CompoundTag tag = stack.getTag();
                BlockPos fromPos = NbtUtils.readBlockPos(tag.getCompound("from"));
                brain.setMemory(ModSetup.ALLAY_SOURCE_TARET.get(), fromPos.above());

                BlockPos toPos = NbtUtils.readBlockPos(tag.getCompound("to"));
                brain.setMemory(ModSetup.ALLAY_DELIVERY_TARET.get(), toPos.above());

                event.setCanceled(true);   
            }
        }
    }
}
