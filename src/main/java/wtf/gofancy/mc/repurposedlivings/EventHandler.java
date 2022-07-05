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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import wtf.gofancy.mc.repurposedlivings.behavior.GoToTargetPosition;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapItem;

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
            Brain<Allay> brain = allay.getBrain();
            brain.memories.put(ModSetup.ALLAY_SOURCE_TARET.get(), Optional.empty());

            Set<Pair<MemoryModuleType<?>, MemoryStatus>> requirements = Stream.concat(
                brain.activityRequirements.get(Activity.IDLE).stream(),
                Stream.<Pair<MemoryModuleType<?>, MemoryStatus>>of(Pair.of(ModSetup.ALLAY_SOURCE_TARET.get(), MemoryStatus.VALUE_ABSENT))
            ).collect(Collectors.toUnmodifiableSet());
            brain.activityRequirements.put(Activity.IDLE, requirements);
            
            brain.addActivity(
                ModSetup.ALLAY_PICK_UP_ITEM.get(),
                10,
                ImmutableList.of(new GoToTargetPosition<>(ModSetup.ALLAY_SOURCE_TARET.get(), 1.75F))
            );
        }
    }
    
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        ItemStack stack = event.getItemStack();
        Entity target = event.getTarget();
        
        if (target instanceof Allay allay && stack.getItem() instanceof AllayMapItem) {
            BlockPos targetPos = NbtUtils.readBlockPos(stack.getTag().getCompound("from"));
            allay.getBrain().setMemory(ModSetup.ALLAY_SOURCE_TARET.get(), targetPos.above());
            
            allay.getBrain().setActiveActivityIfPossible(ModSetup.ALLAY_PICK_UP_ITEM.get());
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if (entity instanceof Allay) {
            // TODO Animate open chest?
            entity.getBrain().getMemory(ModSetup.ALLAY_SOURCE_TARET.get())
                .filter(pos -> entity.blockPosition().closerThan(pos, 0.5))
                .map(pos -> entity.level.getBlockEntity(pos.below()))
                .flatMap(be -> be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).resolve())
                .flatMap(itemHandler -> IntStream.range(0, itemHandler.getSlots())
                    .mapToObj(i -> itemHandler.extractItem(i, Integer.MAX_VALUE, false))
                    .filter(stack -> !stack.isEmpty())
                    .findFirst())
                .ifPresent(stack -> entity.setItemInHand(InteractionHand.MAIN_HAND, stack));
        }
    }
}
