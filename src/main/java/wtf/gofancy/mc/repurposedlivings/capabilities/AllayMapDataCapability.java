package wtf.gofancy.mc.repurposedlivings.capabilities;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraftforge.common.util.INBTSerializable;
import wtf.gofancy.mc.repurposedlivings.item.AllayMapData;

import java.util.Optional;

public interface AllayMapDataCapability extends INBTSerializable<Tag> {

    default Optional<AllayMapData> get(ItemStack stack) {
        return Optional.ofNullable(MapItem.getMapId(stack)).flatMap(this::get);
    }

    default void set(ItemStack stack, AllayMapData data) {
        Optional.ofNullable(MapItem.getMapId(stack)).ifPresent(mapId -> this.set(mapId, data));
    }

    Optional<AllayMapData> get(int mapId);

    void set(int mapId, AllayMapData data);
}
