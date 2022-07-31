package wtf.gofancy.mc.repurposedlivings;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import wtf.gofancy.mc.repurposedlivings.features.allay.entity.network.SetItemInHandPacket;
import wtf.gofancy.mc.repurposedlivings.features.allay.map.network.UpdateAllayMapDataPacket;

import java.util.Optional;

public final class Network {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
        RepurposedLivings.rl("main"),
        () -> PROTOCOL_VERSION,
        PROTOCOL_VERSION::equals,
        PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        int id = 0;

        INSTANCE.registerMessage(id++,
            UpdateAllayMapDataPacket.class,
            UpdateAllayMapDataPacket::encode,
            UpdateAllayMapDataPacket::decode,
            UpdateAllayMapDataPacket::processClientPacket,
            Optional.of(NetworkDirection.PLAY_TO_CLIENT)
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
