package wtf.gofancy.mc.repurposedlivings.capabilities;

import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class AllayMapDataSyncFlagProvider implements ICapabilityProvider, INBTSerializable<Tag> {

    private static class SyncFlag implements AllayMapDataSyncFlagCapability {

        public static final Codec<Set<Integer>> CODEC = Codec.list(Codec.INT).xmap(HashSet::new, ArrayList::new);

        private Set<Integer> syncs = new HashSet<>();

        @Override
        public boolean needsSync(int mapId) {
            return this.syncs.contains(mapId);
        }

        @Override
        public void setNeedsSync(int mapId) {
            this.syncs.add(mapId);
        }

        @Override
        public void setSynced(int mapId) {
            this.syncs.remove(mapId);
        }

        @Override
        public Tag serializeNBT() {
            return CODEC.encodeStart(NbtOps.INSTANCE, this.syncs).getOrThrow(false, str -> {});
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            this.syncs = CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow(false, str -> {});
        }
    }

    private final SyncFlag instance = new SyncFlag();
    private final LazyOptional<AllayMapDataSyncFlagCapability> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == Capabilities.ALLAY_MAP_DATA_SYNC_FLAG) {
            return this.optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public Tag serializeNBT() {
        return this.instance.serializeNBT();
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        this.instance.deserializeNBT(nbt);
    }
}
