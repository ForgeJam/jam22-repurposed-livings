package wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;

public interface AllayMapDataSyncFlagCapability extends INBTSerializable<Tag> {
    ResourceLocation NAME = RepurposedLivings.rl("allay_map_data_sync_flag");

    boolean requiresSync(int mapId);
    void setSynced(int mapId);

    void invalidate(int mapId);
    void invalidateAll();
}
