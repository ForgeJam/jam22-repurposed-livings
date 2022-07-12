package wtf.gofancy.mc.repurposedlivings;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import wtf.gofancy.mc.repurposedlivings.entity.HijackedAllay;
import wtf.gofancy.mc.repurposedlivings.network.Network;

@Mod(RepurposedLivings.MODID)
public class RepurposedLivings {
    public static final String MODID = "repurposedlivings";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RepurposedLivings() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::onEntityAttributeCreation);
        ModSetup.register(bus);
        
        Network.registerPackets();
        
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }
    
    public void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModSetup.HIJACKED_ALLAY_ENTITY.get(), HijackedAllay.createAttributes().build());
    }
}
