package wtf.gofancy.mc.repurposedlivings;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import wtf.gofancy.mc.repurposedlivings.compat.ModProbeProvider;
import wtf.gofancy.mc.repurposedlivings.feature.allay.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability.AllayMapDataCapability;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability.AllayMapDataSyncFlagCapability;

@Mod(RepurposedLivings.MODID)
public class RepurposedLivings {
    public static final String MODID = "repurposedlivings";
    private static final Logger LOGGER = LogUtils.getLogger();
    
    private static final String THEONEPROBE_MODID = "theoneprobe";

    public static ResourceLocation rl(final String path) {
        return new ResourceLocation(MODID, path);
    }

    public RepurposedLivings() {
        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onEntityAttributeCreation);
        bus.addListener(this::registerCapabilities);
        bus.addListener(this::enqueIMC);
        ModSetup.register(bus);
        
        Network.registerPackets();
        
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }
    
    public void onEntityAttributeCreation(final EntityAttributeCreationEvent event) {
        event.put(ModSetup.HIJACKED_ALLAY_ENTITY.get(), HijackedAllay.createAttributes().build());
    }

    public void registerCapabilities(final RegisterCapabilitiesEvent event) {
        event.register(AllayMapDataCapability.class);
        event.register(AllayMapDataSyncFlagCapability.class);
    }
    
    public void enqueIMC(final InterModEnqueueEvent event) {
        if (ModList.get().isLoaded(THEONEPROBE_MODID)) {
            InterModComms.sendTo(THEONEPROBE_MODID, "getTheOneProbe", ModProbeProvider::new);
        }
    }
}
