package wtf.gofancy.mc.repurposedlivings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.Optional;

public final class ModUtil {

    public static CompoundTag createTargetTag(BlockPos pos, Direction side) {
        CompoundTag tag = new CompoundTag();
        CompoundTag posTag = NbtUtils.writeBlockPos(pos);
        tag.put("pos", posTag);
        tag.putInt("side", side.get3DDataValue());
        return tag;
    }

    public static boolean isContainer(Level level, BlockPos pos, Direction side) {
        return Optional.ofNullable(level.getBlockEntity(pos))
            .flatMap(be -> be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).resolve())
            .isPresent();
    }

    private ModUtil() {}
}
