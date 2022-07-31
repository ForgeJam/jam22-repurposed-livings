package wtf.gofancy.mc.repurposedlivings.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import wtf.gofancy.mc.repurposedlivings.RepurposedLivings;

public class TranslationUtils {

    public static MutableComponent generic(final String name, final Object... args) {
        return Component.translatable(String.join(".", RepurposedLivings.MODID, name), args);
    }

    public static MutableComponent item(final Item item, final String name, final Object... args) {
        final ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return Component.translatable(String.join(".", "item", key.getNamespace(), key.getPath(), name), args);
    }

    public static MutableComponent message(final String name, final Object... args) {
        return Component.translatable(String.join(".", "message", RepurposedLivings.MODID, name), args);
    }

    public static MutableComponent tooltip(final Item item, final String name, final Object... args) {
        final ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        return Component.translatable(
            String.join(".", "item", key.getNamespace(), key.getPath(), "tooltip", name),
            args
        );
    }

    public static MutableComponent get(final String prefix, final String name, final Object... args) {
        return Component.translatable(String.join(".", prefix, RepurposedLivings.MODID, name), args);
    }

    private TranslationUtils() {}
}
