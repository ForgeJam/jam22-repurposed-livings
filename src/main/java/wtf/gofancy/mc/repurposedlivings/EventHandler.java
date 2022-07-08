package wtf.gofancy.mc.repurposedlivings;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.entity.behavior.GoToTargetPosition;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapItem;
import wtf.gofancy.mc.repurposedlivings.item.MindControlDeviceItem;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        
        if (entity instanceof Allay allay) {
            Brain<Allay> brain = allay.getBrain();
            brain.memories.put(ModSetup.ALLAY_SOURCE_TARET.get(), Optional.empty());
            brain.memories.put(ModSetup.ALLAY_DELIVERY_TARET.get(), Optional.empty());

            Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements = Stream.concat(
                brain.activityRequirements.get(Activity.IDLE).stream(),
                Stream.<Pair<MemoryModuleType<?>, MemoryStatus>>of(Pair.of(ModSetup.ALLAY_SOURCE_TARET.get(), MemoryStatus.VALUE_ABSENT))
            ).collect(Collectors.toUnmodifiableSet());
            brain.activityRequirements.put(Activity.IDLE, requirements);
            
            brain.addActivity(
                ModSetup.ALLAY_TRANSFER_ITEMS.get(),
                10,
                ImmutableList.of(
                    new GoToTargetPosition<>(ModSetup.ALLAY_SOURCE_TARET.get(), 1.75F, e -> e.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()),
                    new GoToTargetPosition<>(ModSetup.ALLAY_DELIVERY_TARET.get(), 1.75F, Allay::hasItemInHand)
                )
            );
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
                
                if (!event.getPlayer().isCreative()) stack.shrink(1);
            }
            else if (item instanceof AllayMapItem) {
                Brain<Allay> brain = allay.getBrain();
                CompoundTag tag = stack.getTag();
                BlockPos fromPos = NbtUtils.readBlockPos(tag.getCompound("from"));
                brain.setMemory(ModSetup.ALLAY_SOURCE_TARET.get(), fromPos.above());

                BlockPos toPos = NbtUtils.readBlockPos(tag.getCompound("to"));
                brain.setMemory(ModSetup.ALLAY_DELIVERY_TARET.get(), toPos.above());

                brain.setActiveActivityIfPossible(ModSetup.ALLAY_TRANSFER_ITEMS.get());
                event.setCanceled(true);   
            }
        }
    }
    
//    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof Allay) {
            // TODO Find free spot around target, don't rely on above block
            ItemStack stackInHand = entity.getItemInHand(InteractionHand.MAIN_HAND);
            if (stackInHand.isEmpty()) {
                getTargetItemHandler(entity, ModSetup.ALLAY_SOURCE_TARET.get())
                    .flatMap(itemHandler -> IntStream.range(0, itemHandler.getSlots())
                        .mapToObj(i -> itemHandler.extractItem(i, Integer.MAX_VALUE, false))
                        .filter(stack -> !stack.isEmpty())
                        .findFirst())
                    .ifPresent(stack -> entity.setItemInHand(InteractionHand.MAIN_HAND, stack));
            } 
            else {
                getTargetItemHandler(entity, ModSetup.ALLAY_DELIVERY_TARET.get())
                    .map(itemHandler -> insertStack(itemHandler, stackInHand))
                    .ifPresent(stack -> entity.setItemInHand(InteractionHand.MAIN_HAND, stack));
            }
        }
    }
    
    private Optional<IItemHandler> getTargetItemHandler(LivingEntity entity, MemoryModuleType<BlockPos> memory) {
        return entity.getBrain().getMemory(memory)
            .filter(pos -> entity.blockPosition().closerThan(pos, 0.5))
            .map(pos -> entity.level.getBlockEntity(pos.below()))
            .flatMap(be -> be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve());
    }
    
    private ItemStack insertStack(IItemHandler handler, ItemStack stack) {
        ItemStack inserted = stack;
        for (int i = 0; i < handler.getSlots(); i++) {
            inserted = handler.insertItem(i, inserted, false);
        }
        return inserted;
    }
}
