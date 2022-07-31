package wtf.gofancy.mc.repurposedlivings.feature.allay.map.capability;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraftforge.common.util.INBTSerializable;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;
import wtf.gofancy.mc.repurposedlivings.feature.allay.map.AllayMapData;

import java.util.Optional;

public interface AllayMapDataCapability extends INBTSerializable<Tag> {
    ResourceLocation NAME = RepurposedLivings.rl("allay_map_data"); 

    default Optional<AllayMapData> get(final ItemStack stack) {
        return Optional.ofNullable(MapItem.getMapId(stack)).flatMap(this::get);
    }

    default void set(final ItemStack stack, final AllayMapData data) {
        Optional.ofNullable(MapItem.getMapId(stack)).ifPresent(mapId -> this.set(mapId, data));
    }

    Optional<AllayMapData> get(int mapId);

    void set(int mapId, AllayMapData data);
}
