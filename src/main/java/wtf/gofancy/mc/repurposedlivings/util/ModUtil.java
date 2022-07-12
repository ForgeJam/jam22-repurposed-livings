package wtf.gofancy.mc.repurposedlivings.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import wtf.gofancy.mc.repurposedlivings.ModSetup;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;

import java.util.Optional;

public final class ModUtil {
    public static boolean isContainer(Level level, BlockPos pos, Direction side) {
        return Optional.ofNullable(level.getBlockEntity(pos))
            .flatMap(be -> be.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, side).resolve())
            .isPresent();
    }
    
    public static MutableComponent getTranslation(String name, Object... args) {
        return Component.translatable(String.join(".", RepurposedLivings.MODID, name), args);
    }
    
    public static MutableComponent getItemTranslation(Item item, String name, Object... args) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return Component.translatable(String.join(".", "item", key.getNamespace(), key.getPath(), name), args);
    }
    
    public static MutableComponent getContainerTranslation(String container, String name, Object... args) {
        return Component.translatable(String.join(".", "container", RepurposedLivings.MODID, container, name), args);
    }
    
    public static MutableComponent getTargetTranslation(String key, ItemTarget target) {
        BlockPos pos = target.pos();
        return getTranslation(key)
            .append(getItemTranslation(ModSetup.ALLAY_MAP.get(), "pos", pos.getX(), pos.getY(), pos.getZ()))
            .append(", ")
            .append(getItemTranslation(ModSetup.ALLAY_MAP.get(), "side"))
            .append(getItemTranslation(ModSetup.ALLAY_MAP.get(), "side." + target.side().getName()));
    }

    private ModUtil() {}
}
