package wtf.gofancy.mc.repurposedlivings;

import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wtf.gofancy.mc.repurposedlivings.container.AllayMapContainer;
import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapDraftItem;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapItem;
import wtf.gofancy.mc.repurposedlivings.item.EchoMindControlDevice;
import wtf.gofancy.mc.repurposedlivings.item.MindControlDevice;
import wtf.gofancy.mc.repurposedlivings.util.ItemStackListEntityDataSerializer;
import wtf.gofancy.mc.repurposedlivings.util.ItemTarget;

import java.util.Optional;

public final class ModSetup {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RepurposedLivings.MODID);
    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, RepurposedLivings.MODID);
    private static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, RepurposedLivings.MODID);
    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, RepurposedLivings.MODID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, RepurposedLivings.MODID);
    private static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, RepurposedLivings.MODID);
    private static final DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_DATA_SERIALIZERS, RepurposedLivings.MODID);
    
    public static final CreativeModeTab REPURPOSED_LIVINGS_TAB = new CreativeModeTab(RepurposedLivings.MODID) {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ALLAY_MAP.get());
        }
    };
    private static final Item.Properties ITEM_PROPERTIES = new Item.Properties().tab(REPURPOSED_LIVINGS_TAB);
    
    public static final RegistryObject<Item> ALLAY_MAP = ITEMS.register("allay_map", AllayMapItem::new);
    public static final RegistryObject<Item> ALLAY_MAP_DRAFT = ITEMS.register("allay_map_draft", AllayMapDraftItem::new);
    public static final RegistryObject<Item> MIND_CONTROL_DEVICE = ITEMS.register("mind_control_device", () -> new MindControlDevice(ITEM_PROPERTIES));
    public static final RegistryObject<Item> ECHO_MIND_CONTROL_DEVICE = ITEMS.register("echo_mind_control_device", () -> new EchoMindControlDevice(ITEM_PROPERTIES));
    public static final RegistryObject<Item> ECHO_PEARL = ITEMS.register("echo_pearl", () -> new Item(ITEM_PROPERTIES));
    
    public static final RegistryObject<MemoryModuleType<ItemTarget>> ALLAY_SOURCE_TARET = MEMORY_MODULE_TYPES.register("allay_source_target", () -> new MemoryModuleType<>(Optional.of(ItemTarget.CODEC)));
    public static final RegistryObject<MemoryModuleType<ItemTarget>> ALLAY_DELIVERY_TARET = MEMORY_MODULE_TYPES.register("allay_delivery_target", () -> new MemoryModuleType<>(Optional.of(ItemTarget.CODEC)));
    
    public static final RegistryObject<Activity> ALLAY_TRANSFER_ITEMS = ACTIVITIES.register("allay_transfer_items", () -> new Activity("allay_transfer_items"));
    
    public static final RegistryObject<EntityType<HijackedAllay>> HIJACKED_ALLAY_ENTITY = ENTITY_TYPES.register("hijacked_allay", () -> EntityType.Builder.of(HijackedAllay::new, MobCategory.CREATURE)
        .sized(0.35F, 0.6F)
        .clientTrackingRange(8)
        .updateInterval(2)
        .build("hijacked_allay"));
    
    public static final RegistryObject<SoundEvent> MIND_CONTROL_DEVICE_ATTACH_SOUND = SOUNDS.register("mind_control_device_attach", () -> new SoundEvent(new ResourceLocation(RepurposedLivings.MODID, "mind_control_device_attach")));
    
    public static final RegistryObject<MenuType<AllayMapContainer>> POWERGEN_CONTAINER = MENU_TYPES.register("powergen",
            () -> IForgeMenuType.create((windowId, inv, data) -> new AllayMapContainer(windowId, data.readEnum(InteractionHand.class), inv.player)));
    
    public static final RegistryObject<EntityDataSerializer<NonNullList<ItemStack>>> ITEM_STACK_LIST_SERIALIZER = ENTITY_DATA_SERIALIZERS.register("item_stack_list", ItemStackListEntityDataSerializer::new);
    
    static void register(IEventBus bus) {
        ITEMS.register(bus);
        MEMORY_MODULE_TYPES.register(bus);
        ACTIVITIES.register(bus);
        ENTITY_TYPES.register(bus);
        SOUNDS.register(bus);
        MENU_TYPES.register(bus);
        ENTITY_DATA_SERIALIZERS.register(bus);
    }
    
    private ModSetup() {}
}
