package wtf.gofancy.mc.repurposedlivings;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RepurposedLivings.MODID)
public class RepurposedLivings {
    public static final String MODID = "repurposedlivings";
    private static final Logger LOGGER = LogUtils.getLogger();

    public RepurposedLivings() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSetup.register(bus);
        
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }
}
