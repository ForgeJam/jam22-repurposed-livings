package wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wtf.gofancy.mc.repurposedlivings.Capabilities;

public class AllayMapDataStorageProvider implements ICapabilityProvider, INBTSerializable<Tag> {

    private final AllayMapDataCapability instance = new AllayMapDataStorage();
    private final LazyOptional<AllayMapDataCapability> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return Capabilities.ALLAY_MAP_DATA.orEmpty(cap, this.optional);
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
