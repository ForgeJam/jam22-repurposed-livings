package wtf.gofancy.mc.repurposedlivings;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability.AllayMapDataCapability;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability.AllayMapDataSyncFlagCapability;
import wtf.gofancy.mc.repurposedlivings.feature.allay.entity.HijackedAllay;

@Mod(RepurposedLivings.MODID)
public class RepurposedLivings {
    public static final String MODID = "repurposedlivings";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation rl(final String path) {
        return new ResourceLocation(MODID, path);
    }

    public RepurposedLivings() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onEntityAttributeCreation);
        bus.addListener(this::registerCapabilities);
        ModSetup.register(bus);
        
        Network.registerPackets();
        
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }
    
    public void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModSetup.HIJACKED_ALLAY_ENTITY.get(), HijackedAllay.createAttributes().build());
    }

    public void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(AllayMapDataCapability.class);
        event.register(AllayMapDataSyncFlagCapability.class);
    }
}
