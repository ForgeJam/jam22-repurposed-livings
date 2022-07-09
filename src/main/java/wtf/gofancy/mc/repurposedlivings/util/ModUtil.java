package wtf.gofancy.mc.repurposedlivings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;

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
    
    public static MutableComponent getTranslation(String name, Object... args) {
        return Component.translatable(String.join(".", RepurposedLivings.MODID, name), args);
    }
    
    public static MutableComponent getItemTranslation(Item item, String name) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return Component.translatable(String.join(".", "item", key.getNamespace(), key.getPath(), name));
    }

    private ModUtil() {}
}
