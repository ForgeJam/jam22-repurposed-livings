package wtf.gofancy.mc.repurposedlivings.capabilities;

import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

public interface AllayMapDataSyncFlagCapability extends INBTSerializable<Tag> {

    boolean needsSync(int mapId);
    void setNeedsSync(int mapId);
    void setSynced(int mapId);
}
