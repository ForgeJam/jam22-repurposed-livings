package wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.AllayMapData;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AllayMapDataStorage implements AllayMapDataCapability {
    public static final Codec<Map<Integer, AllayMapData>> CODEC = Codec.unboundedMap(Codec.STRING.xmap(Integer::valueOf, String::valueOf), AllayMapData.CODEC);

    private Map<Integer, AllayMapData> storage;

    public AllayMapDataStorage() {
        this.storage = new HashMap<>();
    }

    @Override
    public Optional<AllayMapData> get(final int mapId) {
        return Optional.ofNullable(this.storage.get(mapId));
    }

    @Override
    public void set(final int mapId, final AllayMapData data) {
        this.storage.put(mapId, data);
    }

    @Override
    public Tag serializeNBT() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this.storage).getOrThrow(false, msg -> {});
    }

    @Override
    public void deserializeNBT(final Tag nbt) {
        this.storage = new HashMap<>(CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow(false, msg -> {}));
    }
}
