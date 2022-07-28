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
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
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
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.LIKED_PLAYER,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, ModSetup.ALLAY_SOURCE_TARET.get(), ModSetup.ALLAY_DELIVERY_TARET.get()
    );
    protected static final EntityDataAccessor<NonNullList<ItemStack>> EQUIPMENT_SLOTS = SynchedEntityData.defineId(HijackedAllay.class, ModSetup.ITEM_STACK_LIST_SERIALIZER.get());

    public HijackedAllay(EntityType<? extends HijackedAllay> type, Level level) {
        super(type, level);
    }
    
    public ItemStack getItemInSlot(AllayEquipment slot) {
        return getEquipmentSlots().get(slot.ordinal());
    }

    public NonNullList<ItemStack> getEquipmentSlots() {
        return this.entityData.get(EQUIPMENT_SLOTS);
    }

    public void setEquipmentSlot(AllayEquipment slot, ItemStack stack) {
        int index = slot.ordinal();
        ItemStack result = getEquipmentSlots().set(index, stack);
        boolean empty = stack.isEmpty() && result.isEmpty();
        if (!empty && !ItemStack.isSameIgnoreDurability(stack, result)) {
            playEquipSound(stack);
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
        // Disable automatic item pickup
        return false;
    }

    @Override
    public boolean isDancing() {
        // Hijacked allays can't dance
        return false;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getMainHandItem();
        ItemStack map = getItemInSlot(AllayEquipment.MAP);
        // Un-hijack allay by shift-clicking it with an empty hand
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            dropEquipment();
            if (this.level instanceof ServerLevel serverLevel) {
                // Replace hijacked allay with a normal one
                Allay allay = new Allay(EntityType.ALLAY, this.level);
                // Copy position and body rotation
                allay.moveTo(position());
                allay.setXRot(getXRot());
                allay.setYRot(getYRot());
                allay.setOldPosAndRot();
                allay.setYBodyRot(getYRot());
                allay.setYHeadRot(getYRot());
                allay.setPersistenceRequired();

                remove(RemovalReason.DISCARDED);
                this.level.addFreshEntity(allay);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, getX(), getY() + 0.2, getZ(), 10, 0.25, 0.25, 0.25, 0);
            }
            return InteractionResult.SUCCESS;
        }
        // Give the Allay an Allay Map
        else if (stack.getItem() == ModSetup.ALLAY_MAP.get() && map.isEmpty()) {
            CompoundTag tag = stack.getTag();
            ItemTarget from = ItemTarget.fromNbt(tag.getCompound("from"));
            this.brain.setMemory(ModSetup.ALLAY_SOURCE_TARET.get(), from);
            ItemTarget to = ItemTarget.fromNbt(tag.getCompound("to"));
            this.brain.setMemory(ModSetup.ALLAY_DELIVERY_TARET.get(), to);
            this.brain.setActiveActivityToFirstValid(List.of(ModSetup.ALLAY_TRANSFER_ITEMS.get()));
            
            setEquipmentSlot(AllayEquipment.MAP, stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }
        // Take an Allay Map from the Allay
        else if (stack.isEmpty() && !map.isEmpty()) {
            this.brain.eraseMemory(ModSetup.ALLAY_SOURCE_TARET.get());
            this.brain.eraseMemory(ModSetup.ALLAY_DELIVERY_TARET.get());
            this.brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            
            dropItem(getItemInHand(InteractionHand.MAIN_HAND));
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            
            setEquipmentSlot(AllayEquipment.MAP, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, map);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        ItemStack stackInHand = getItemInHand(InteractionHand.MAIN_HAND);
        if (stackInHand.isEmpty()) {
            // Search for the source container in range
            getTargetItemHandler(ModSetup.ALLAY_SOURCE_TARET.get(), 2)
                // Try extracting the first item
                .flatMap(itemHandler -> IntStream.range(0, itemHandler.getSlots())
                    .mapToObj(i -> itemHandler.extractItem(i, Integer.MAX_VALUE, false))
                    .filter(stack -> !stack.isEmpty())
                    .findFirst())
                // Add it to the allay's hand
                .ifPresent(stack -> setItemInHand(InteractionHand.MAIN_HAND, stack));
        } else {
            // Search for the destination container in range
            getTargetItemHandler(ModSetup.ALLAY_DELIVERY_TARET.get(), 1)
                // Insert the item from our hand into the container
                .map(itemHandler -> insertStack(itemHandler, stackInHand))
                // Keep the remainder (if any) in our hand to insert when more space becomes available
                .ifPresent(stack -> setItemInHand(InteractionHand.MAIN_HAND, stack));
        }
    }

    @Override
    public void dropEquipment() {
        super.dropEquipment();
        
        // Drop all items from equipment slots
        getEquipmentSlots().forEach(stack -> {
            spawnAtLocation(stack.copy());
            stack.setCount(0);
        });
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);

        // Save equipment slots to NBT
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
        
        // Load equipment slots from NBT
        if (tag.contains("EquipmentSlots", Tag.TAG_LIST)) {
            ListTag list = tag.getList("EquipmentSlots", Tag.TAG_COMPOUND);

            NonNullList<ItemStack> equipmentSlots = getEquipmentSlots();
            for (int i = 0; i < equipmentSlots.size(); ++i) {
                equipmentSlots.set(i, ItemStack.of(list.getCompound(i)));
            }
        }
    }

    /**
     * Find an IItemHandler at a target position with a range limit.
     * Additionally, the Allay must be looking at the target to emulate a real container interation.
     * 
     * @param memory the ItemTarget value holder
     * @param range the range to search in
     * @return optional IItemHandler
     */
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

    /**
     * Insert an ItemStack into an IItemHandler, returning the remainder if there's any.
     * 
     * @param handler the container to insert into
     * @param stack the stack to insert
     * @return the remainer, or an empty stack if there's none
     */
    private ItemStack insertStack(IItemHandler handler, ItemStack stack) {
        ItemStack inserted = stack;
        for (int i = 0; i < handler.getSlots(); i++) {
            inserted = handler.insertItem(i, inserted, false);
        }
        return inserted;
    }

    /**
     * Drop an item at the Allay's location with no delta movement.
     * 
     * @param stack the item to drop
     */
    private void dropItem(ItemStack stack) {
        ItemEntity entity = new ItemEntity(this.level, getX(), getY(), getZ(), stack);
        entity.setDeltaMovement(0, 0, 0);
        this.level.addFreshEntity(entity);
    }
}
