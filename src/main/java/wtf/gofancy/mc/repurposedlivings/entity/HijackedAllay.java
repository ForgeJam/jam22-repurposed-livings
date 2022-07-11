package wtf.gofancy.mc.repurposedlivings.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class HijackedAllay extends Allay {
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.LIKED_PLAYER,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, ModSetup.ALLAY_SOURCE_TARET.get(), ModSetup.ALLAY_DELIVERY_TARET.get()
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
        return Brain.provider(MEMORY_TYPES, List.of());
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
            getTargetItemHandler(ModSetup.ALLAY_SOURCE_TARET.get(), 2)
                .flatMap(itemHandler -> IntStream.range(0, itemHandler.getSlots())
                    .mapToObj(i -> itemHandler.extractItem(i, Integer.MAX_VALUE, false))
                    .filter(stack -> !stack.isEmpty())
                    .findFirst())
                .ifPresent(stack -> setItemInHand(InteractionHand.MAIN_HAND, stack));
        } else {
            getTargetItemHandler(ModSetup.ALLAY_DELIVERY_TARET.get(), 1)
                .map(itemHandler -> insertStack(itemHandler, stackInHand))
                .ifPresent(stack -> setItemInHand(InteractionHand.MAIN_HAND, stack));
        }
    }

    @Override
    public void dropEquipment() {
        super.dropEquipment();
        this.equipmentSlots.forEach(this::spawnAtLocation);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        ListTag list = new ListTag();
        for (ItemStack stack : this.equipmentSlots) {
            CompoundTag stackTag = new CompoundTag();
            if (!stack.isEmpty()) stack.save(stackTag);
            list.add(stackTag);
        }
        tag.put("EquipmentSlots", list);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        
        if (tag.contains("EquipmentSlots", Tag.TAG_LIST)) {
            ListTag list = tag.getList("EquipmentSlots", Tag.TAG_COMPOUND);

            for (int i = 0; i < this.equipmentSlots.size(); ++i) {
                this.equipmentSlots.set(i, ItemStack.of(list.getCompound(i)));
            }
        }
    }

    private Optional<IItemHandler> getTargetItemHandler(MemoryModuleType<ItemTarget> memory, double range) {
        return this.brain.getMemory(memory)
            .filter(target -> {
                BlockPos pos = target.pos();
                HitResult result = this.level.clip(new ClipContext(this.position(), new Vec3(pos.getX(), pos.getY(), pos.getZ()), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, this));
                return result instanceof BlockHitResult blockHit && blockHit.getDirection() == target.side() && this.blockPosition().distSqr(pos) <= Mth.square(range);
            })
            .map(target -> this.level.getBlockEntity(target.pos()))
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
