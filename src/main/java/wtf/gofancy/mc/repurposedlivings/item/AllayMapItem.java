package wtf.gofancy.mc.repurposedlivings.item;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.Item;

public class AllayMapItem extends Item {

    public AllayMapItem() {
        super(new Properties().stacksTo(1));
    }
    
    public static CompoundTag createTarget(BlockPos from, BlockPos to) {
        CompoundTag tag = new CompoundTag();
        tag.put("from", NbtUtils.writeBlockPos(from));
        tag.put("to", NbtUtils.writeBlockPos(to));
        return tag;
    }
}
