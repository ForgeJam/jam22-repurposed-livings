package wtf.gofancy.mc.repurposedlivings.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
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

import java.util.Optional;
import java.util.stream.IntStream;

public class HijackedAllay extends Allay {
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(SensorType.HURT_BY);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, 
        MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.LIKED_PLAYER,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, ModSetup.ALLAY_SOURCE_TARET.get(), ModSetup.ALLAY_DELIVERY_TARET.get()
    );
    protected static final EntityDataAccessor<NonNullList<ItemStack>> EQUIPMENT_SLOTS = SynchedEntityData.defineId(HijackedAllay.class, ModSetup.ITEM_STACK_LIST_SERIALIZER.get());

    public HijackedAllay(EntityType<? extends HijackedAllay> type, Level level) {
        super(type, level);
    }

    public NonNullList<ItemStack> getEquipmentSlots() {
        return this.entityData.get(EQUIPMENT_SLOTS);
    }

    public ItemStack getItemInSlot(AllayEquipment slot) {
        return getEquipmentSlots().get(slot.ordinal());
    }

    public void setEquipmentSlot(AllayEquipment slot, ItemStack stack) {
        this.verifyEquippedItem(stack);
        int index = slot.ordinal();
        if (!stack.isEmpty() && !ItemStack.isSameIgnoreDurability(stack, getEquipmentSlots().set(index, stack))) {
            this.playEquipSound(stack);
        }
    }

    @Override
    protected Brain.Provider<Allay> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        Brain<?> brain = brainProvider().makeBrain(dynamic);
        return HijackedAllayAi.createBrain((Brain<HijackedAllay>) brain);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EQUIPMENT_SLOTS, NonNullList.withSize(AllayEquipment.values().length, ItemStack.EMPTY));
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

    public void onPanicStopped() {
        if (this.level.random.nextInt(100) < 25) {
            onHijackActivated();
        }
        else {
            dropEquipment();
            Allay allay = new Allay(EntityType.ALLAY, this.level);
            allay.moveTo(this.position());
            allay.setPersistenceRequired();
            
            remove(RemovalReason.DISCARDED);
            this.level.addFreshEntity(allay);
        }
    }
    
    public void onHijackActivated() {
        if (this.level instanceof ServerLevel serverLevel) {
            this.brain.useDefaultActivity();
            serverLevel.sendParticles(ParticleTypes.WITCH, getX(), getY() + 0.2, getZ(), 30, 0.35, 0.35, 0.35, 0);
            this.level.playSound(null, this, ModSetup.MIND_CONTROL_DEVICE_ATTACH_SOUND.get(), SoundSource.MASTER, 1, 1);
        }
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
        getEquipmentSlots().forEach(this::spawnAtLocation);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        ListTag list = new ListTag();
        for (ItemStack stack : getEquipmentSlots()) {
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

            NonNullList<ItemStack> equipmentSlots = getEquipmentSlots();
            for (int i = 0; i < equipmentSlots.size(); ++i) {
                equipmentSlots.set(i, ItemStack.of(list.getCompound(i)));
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
