package wtf.gofancy.mc.repurposedlivings.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

import java.util.Optional;
import java.util.stream.IntStream;

public class HijackedAllay extends Allay {
    public static final double PICKUP_RANGE = 0.75;
    // TODO Cleanup
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.WALK_TARGET,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.LIKED_PLAYER,
        MemoryModuleType.IS_PANICKING, ModSetup.ALLAY_SOURCE_TARET.get(), ModSetup.ALLAY_DELIVERY_TARET.get()
    );
    
    private final NonNullList<ItemStack> equipmentSlots = NonNullList.withSize(4, ItemStack.EMPTY);

    public HijackedAllay(EntityType<? extends HijackedAllay> type, Level level) {
        super(type, level);
    }

    public void setEquipmentSlot(AllayEquipment slot, ItemStack stack) {
        this.verifyEquippedItem(stack);
        int index = slot.ordinal();
        if (!stack.isEmpty() && !ItemStack.isSameIgnoreDurability(stack, this.equipmentSlots.set(index, stack))) {
            this.playEquipSound(stack);
        }
    }

    @Override
    protected Brain.Provider<Allay> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return HijackedAllayAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public boolean wantsToPickUp(ItemStack stack) {
        return false;
    }
    
    @Override
    public boolean shouldListen(ServerLevel level, GameEventListener listener, BlockPos pos, GameEvent event, GameEvent.Context context) {
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        ItemStack stackInHand = getItemInHand(InteractionHand.MAIN_HAND);
        if (stackInHand.isEmpty()) {
            getTargetItemHandler(ModSetup.ALLAY_SOURCE_TARET.get())
                .flatMap(itemHandler -> IntStream.range(0, itemHandler.getSlots())
                    .mapToObj(i -> itemHandler.extractItem(i, Integer.MAX_VALUE, false))
                    .filter(stack -> !stack.isEmpty())
                    .findFirst())
                .ifPresent(stack -> setItemInHand(InteractionHand.MAIN_HAND, stack));
        } else {
            getTargetItemHandler(ModSetup.ALLAY_DELIVERY_TARET.get())
                .map(itemHandler -> insertStack(itemHandler, stackInHand))
                .ifPresent(stack -> setItemInHand(InteractionHand.MAIN_HAND, stack));
        }
    }

    @Override
    public void dropEquipment() {
        super.dropEquipment();
        this.equipmentSlots.forEach(this::spawnAtLocation);
    }

    private Optional<IItemHandler> getTargetItemHandler(MemoryModuleType<ItemTarget> memory) {
        return this.brain.getMemory(memory)
            .map(ItemTarget::pos)
            .filter(pos -> this.blockPosition().closerThan(pos, PICKUP_RANGE))
            .map(pos -> this.level.getBlockEntity(pos))
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
