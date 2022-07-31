package wtf.gofancy.mc.repurposedlivings.feature.allay.map.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import wtf.gofancy.mc.repurposedlivings.ModSetup;

public class AllayMapCloneRecipe extends CustomRecipe {

    public AllayMapCloneRecipe(ResourceLocation id) {
        super(id);
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        int cloneCount = 0;
        ItemStack map = ItemStack.EMPTY;

        for (int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack current = container.getItem(j);

            if (current.isEmpty()) continue;

            if (current.is(ModSetup.ALLAY_MAP.get())) {
                if (map.isEmpty()) {
                    map = current;
                } else return false; // more than one map in crafting grid
            } else if (current.is(Items.MAP)) {
                cloneCount++;
            } else return false; // some not-map in crafting grid
        }

        return !map.isEmpty() && cloneCount > 0;
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        int cloneCount = 0;
        ItemStack map = ItemStack.EMPTY;

        for (int j = 0; j < container.getContainerSize(); ++j) {
            ItemStack current = container.getItem(j);

            if (current.isEmpty()) continue;

            if (current.is(ModSetup.ALLAY_MAP.get())) {
                if (map.isEmpty()) {
                    map = current;
                } else return ItemStack.EMPTY; // more than one map in crafting grid
            } else if (current.is(Items.MAP)) {
                cloneCount++;
            } else return ItemStack.EMPTY; // some not-map in crafting grid
        }

        if (map.isEmpty() || cloneCount == 0) return ItemStack.EMPTY;

        if (!map.isEmpty() && cloneCount > 0) {
            ItemStack copy = map.copy();
            copy.setCount(cloneCount + 1);
            return copy;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 3 && height >= 3;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModSetup.ALLAY_MAP_CLONE_RECIPE_SERIALIZER.get();
    }
}
