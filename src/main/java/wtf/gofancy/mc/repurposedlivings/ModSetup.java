package wtf.gofancy.mc.repurposedlivings;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapItem;

import java.util.Optional;

public final class ModSetup {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, RepurposedLivings.MODID);
    private static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(ForgeRegistries.MEMORY_MODULE_TYPES, RepurposedLivings.MODID);
    private static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(ForgeRegistries.ACTIVITIES, RepurposedLivings.MODID);
    
    public static final RegistryObject<Item> ALLAY_MAP = ITEMS.register("allay_map", AllayMapItem::new);
    
    public static final RegistryObject<MemoryModuleType<BlockPos>> ALLAY_SOURCE_TARET = MEMORY_MODULE_TYPES.register("allay_source_target", () -> new MemoryModuleType<>(Optional.empty()));
    public static final RegistryObject<MemoryModuleType<BlockPos>> ALLAY_DELIVERY_TARET = MEMORY_MODULE_TYPES.register("allay_delivery_target", () -> new MemoryModuleType<>(Optional.empty()));
    
    public static final RegistryObject<Activity> ALLAY_TRANSFER_ITEM = ACTIVITIES.register("allay_transfer_item", () -> new Activity("allay_transfer_item"));
    
    static void register(IEventBus bus) {
        ITEMS.register(bus);
        MEMORY_MODULE_TYPES.register(bus);
        ACTIVITIES.register(bus);
    }
    
    private ModSetup() {}
}
