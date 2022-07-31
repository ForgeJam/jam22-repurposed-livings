package wtf.gofancy.mc.repurposedlivings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.List;
import java.util.Optional;

public final class ModUtil {
    public static boolean isContainer(Level level, BlockPos pos, Direction side) {
        return Optional.ofNullable(level.getBlockEntity(pos))
            .flatMap(be -> be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).resolve())
            .isPresent();
    }

    public static void updateContainerContent(Container container, List<ItemStack> stacks) {
        int containerSize = container.getContainerSize();
        if (containerSize != stacks.size()) {
            throw new IllegalStateException("Invalid container size " + container.getContainerSize() + ", expected " + stacks.size());
        }
        for (int i = 0; i < containerSize; i++) {
            container.setItem(i, stacks.get(i));
        }
    }

    private ModUtil() {}
}
