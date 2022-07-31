package wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public interface AllayMapDataSyncFlagCapability extends INBTSerializable<Tag> {

    boolean requiresSync(int mapId);
    void setSynced(int mapId);

    void invalidate(int mapId);
    void invalidateAll();
}
