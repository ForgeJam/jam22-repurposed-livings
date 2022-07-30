package wtf.gofancy.mc.repurposedlivings.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;

import java.util.Optional;

public final class Network {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        new ResourceLocation(RepurposedLivings.MODID, "main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        int id = 0;

        INSTANCE.registerMessage(id++,
            UpdateAllayMapTarget.class,
            UpdateAllayMapTarget::encode,
            UpdateAllayMapTarget::decode,
            UpdateAllayMapTarget::processServerPacket,
            Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        INSTANCE.registerMessage(id++,
            SetItemInHandPacket.class,
            SetItemInHandPacket::encode,
            SetItemInHandPacket::decode,
            SetItemInHandPacket::processClientPacket,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    private Network() {}
}
