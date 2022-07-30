package wtf.gofancy.mc.repurposedlivings.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.PacketDistributor;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.capabilities.Capabilities;
import wtf.gofancy.mc.repurposedlivings.network.Network;
import wtf.gofancy.mc.repurposedlivings.network.SetItemInHandPacket;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;
import wtf.gofancy.mc.repurposedlivings.util.ModUtil;
import wtf.gofancy.mc.repurposedlivings.util.TranslationUtils;

import java.util.Optional;

public class HijackedAllay extends Allay {
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super Allay>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
        MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
        MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.LIKED_PLAYER,
        MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, ModSetup.ALLAY_SOURCE_TARET.get(), ModSetup.ALLAY_DELIVERY_TARET.get()
    );
    protected static final EntityDataAccessor<NonNullList<ItemStack>> EQUIPMENT_SLOTS = SynchedEntityData.defineId(HijackedAllay.class, ModSetup.ITEM_STACK_LIST_SERIALIZER.get());

    /**
     * Extra inventory space, enabled by applying the {@link ModSetup#ENDER_STORAGE_UPGRADE Storage Upgrade}
     */
    private final SimpleContainer extendedInventory = new SimpleContainer(3);

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
                serverLevel.sendParticles(
                        ParticleTypes.HAPPY_VILLAGER,
                        getX(),
                        getY() + 0.2,
                        getZ(),
                        10,
                        0.25,
                        0.25,
                        0.25,
                        0
                );
            }
            return InteractionResult.SUCCESS;
        // Give the Allay an Allay Map
        } else if (stack.is(ModSetup.ALLAY_MAP.get()) && map.isEmpty()) {
            final var data = player.level.getCapability(Capabilities.ALLAY_MAP_DATA)
                    .resolve()
                    .orElseThrow()
                    .get(stack)
                    .orElseThrow();

            if (!data.isComplete()) {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(TranslationUtils.message("allay_map_incomplete").withStyle(ChatFormatting.RED), true);
                }
                return InteractionResult.CONSUME;
            }

            this.brain.setMemory(ModSetup.ALLAY_SOURCE_TARET.get(), data.getSource().orElseThrow());
            this.brain.setMemory(ModSetup.ALLAY_DELIVERY_TARET.get(), data.getDestination().orElseThrow());

            this.brain.setActiveActivityIfPossible(ModSetup.ALLAY_TRANSFER_ITEMS.get());

            setEquipmentSlot(AllayEquipment.MAP, stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }
        // Take an Allay Map from the Allay
        else if (stack.isEmpty() && !map.isEmpty()) {
            this.brain.eraseMemory(ModSetup.ALLAY_SOURCE_TARET.get());
            this.brain.eraseMemory(ModSetup.ALLAY_DELIVERY_TARET.get());
            this.brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            
            spawnAtLocation(getItemInHand(InteractionHand.MAIN_HAND));
            setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            
            setEquipmentSlot(AllayEquipment.MAP, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.MAIN_HAND, map);
            return InteractionResult.SUCCESS;
        }
        // Apply the Storage Upgrade to the Allay
        else if (stack.getItem() == ModSetup.ENDER_STORAGE_UPGRADE.get() && getItemInSlot(AllayEquipment.STORAGE).isEmpty()) {
            setEquipmentSlot(AllayEquipment.STORAGE, stack);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    // Not all BEs sync their content to the client, so we handle insertion/extraction
    // on the server and only sync the item in the Allay's hand
    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        
        if (!hasItemInHand()) {
            // Search for the source container in range
            getTargetItemHandler(ModSetup.ALLAY_SOURCE_TARET.get(), 2)
                // Extract items into our inventory
                .ifPresent(this::pickupItems);
        } else {
            // Search for the destination container in range
            getTargetItemHandler(ModSetup.ALLAY_DELIVERY_TARET.get(), 1)
                // Recursively insert items into the container from our inventory
                .ifPresent(this::deliverItems);
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
        // Drop all items from extended inventory
        this.extendedInventory.removeAllItems().forEach(this::spawnAtLocation);
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
        
        // Save extended inventory to NBT
        tag.put("Inventory", this.extendedInventory.createTag());
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
        
        // Load extended inventory from NBT
        this.extendedInventory.fromTag(tag.getList("Inventory", 10));
        
        if (!getItemInSlot(AllayEquipment.MAP).isEmpty()) {
            this.brain.setActiveActivityIfPossible(ModSetup.ALLAY_TRANSFER_ITEMS.get());
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

    private boolean hasStorageUpgrade() {
        return !getItemInSlot(AllayEquipment.STORAGE).isEmpty();
    }

    /**
     * Extract items from a container into the Allay's hand and inventory (if storage upgrade is applied).
     * 
     * @param itemHandler the container to extract items from
     */
    private void pickupItems(IItemHandler itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.extractItem(i, Integer.MAX_VALUE, false);
            if (!stack.isEmpty()) {
                if (!hasItemInHand()) {
                    setItemInHandSynced(stack);
                } else if (hasStorageUpgrade() && this.extendedInventory.canAddItem(stack)) {
                    this.extendedInventory.addItem(stack);
                } else break;
            }
        }
    }

    /**
     * Insert a single stack from the Allay's hand into the container,
     * then add the remainder back into its hand to be inserted next tick.
     * 
     * @param itemHandler the container to insert items into
     */
    private void deliverItems(IItemHandler itemHandler) {
        ItemStack itemInHand = getItemInHand(InteractionHand.MAIN_HAND);
        // Try inserting the item from our hand
        ItemStack remainder = insertStack(itemHandler, itemInHand);
        // If there's no remainder, set it to the next item from our inventory to be inserted next tick
        if (remainder.isEmpty() && hasStorageUpgrade()) {
            for (int i = 0; i < this.extendedInventory.getContainerSize(); i++) {
                ItemStack next = this.extendedInventory.removeItemNoUpdate(i);
                if (!next.isEmpty()) {
                    remainder = next;
                    break;
                }
            }
        }
        // Add the remainder to the Allay's hand
        setItemInHandSynced(remainder);
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
        for (int i = 0; i < handler.getSlots() && !inserted.isEmpty(); i++) {
            inserted = handler.insertItem(i, inserted, false);
        }
        return inserted;
    }
    
    private void setItemInHandSynced(ItemStack stack) {
        setItemInHand(InteractionHand.MAIN_HAND, stack);
        Network.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> this), new SetItemInHandPacket(getId(), stack));
    }
}
