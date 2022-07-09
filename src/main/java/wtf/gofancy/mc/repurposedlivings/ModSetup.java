package wtf.gofancy.mc.repurposedlivings;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapItem;
import wtf.gofancy.mc.repurposedlivings.item.MindControlDeviceItem;

import java.util.Optional;

public final class ModSetup {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RepurposedLivings.MODID);
    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, RepurposedLivings.MODID);
    private static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, RepurposedLivings.MODID);
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, RepurposedLivings.MODID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RepurposedLivings.MODID);
    
    public static final CreativeModeTab REPURPOSED_LIVINGS_TAB = new CreativeModeTab(RepurposedLivings.MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(Items.APPLE);
        }
    };
    private static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(REPURPOSED_LIVINGS_TAB);
    
    public static final RegistryObject<Item> ALLAY_MAP = ITEMS.register("allay_map", AllayMapItem::new);
    public static final RegistryObject<Item> MIND_CONTROL_DEVICE = ITEMS.register("mind_control_device", () -> new MindControlDeviceItem(ITEM_PROPERTIES));
    
    public static final RegistryObject<MemoryModuleType<BlockPos>> ALLAY_SOURCE_TARET = MEMORY_MODULE_TYPES.register("allay_source_target", () -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));
    public static final RegistryObject<MemoryModuleType<BlockPos>> ALLAY_DELIVERY_TARET = MEMORY_MODULE_TYPES.register("allay_delivery_target", () -> new MemoryModuleType<>(Optional.of(BlockPos.CODEC)));
    
    public static final RegistryObject<Activity> ALLAY_TRANSFER_ITEMS = ACTIVITIES.register("allay_transfer_items", () -> new Activity("allay_transfer_items"));
    
    public static final RegistryObject<EntityType<HijackedAllay>> HIJACKED_ALLAY_ENTITY = ENTITIES.register("hijacked_allay", () -> EntityType.Builder.of(HijackedAllay::new, MobCategory.CREATURE)
        .sized(0.35F, 0.6F)
        .clientTrackingRange(8)
        .updateInterval(2)
        .build("hijacked_allay"));
    
    public static final RegistryObject<SoundEvent> MIND_CONTROL_DEVICE_ATTACH_SOUND = SOUNDS.register("mind_control_device_attach", () -> new SoundEvent(new ResourceLocation(RepurposedLivings.MODID, "mind_control_device_attach")));
    
    static void register(IEventBus bus) {
        ITEMS.register(bus);
        MEMORY_MODULE_TYPES.register(bus);
        ACTIVITIES.register(bus);
        ENTITIES.register(bus);
        SOUNDS.register(bus);
    }
    
    private ModSetup() {}
}
