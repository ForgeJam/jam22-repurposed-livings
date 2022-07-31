package wtf.gofancy.mc.repurposedlivings.feature.allay.map.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import wtf.gofancy.mc.repurposedlivings.Capabilities;
import wtf.gofancy.mc.repurposedlivings.ModSetup;

public class AllayMapExtendingRecipe extends ShapedRecipe {

    public AllayMapExtendingRecipe(ResourceLocation pId) {
        super(pId, "", 3, 3, NonNullList.of(
                Ingredient.EMPTY,
                Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER),
                Ingredient.of(Items.PAPER), Ingredient.of(ModSetup.ALLAY_MAP.get()), Ingredient.of(Items.PAPER),
                Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER), Ingredient.of(Items.PAPER)
        ), new ItemStack(Items.MAP));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModSetup.ALLAY_MAP_EXTENDING_RECIPE_SERIALIZER.get();
    }

    @Override
    public boolean matches(CraftingContainer container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        } else {
            ItemStack map = ItemStack.EMPTY;

            for(int i = 0; i < container.getContainerSize() && map.isEmpty(); ++i) {
                ItemStack current = container.getItem(i);
                if (current.is(ModSetup.ALLAY_MAP.get())) {
                    map = current;
                }
            }

            if (map.isEmpty() || level.isClientSide()) {
                return false;
            } else {
                // this check is serverside only (would fail on client side anyway)
                final MapItemSavedData data = level.getCapability(Capabilities.ALLAY_MAP_DATA)
                        .resolve()
                        .orElseThrow()
                        .get(map)
                        .orElseThrow()
                        .getCorrespondingMapData(level);

                return data.scale < 4;
            }
        }
    }

    @Override
    public ItemStack assemble(CraftingContainer container) {
        ItemStack map = ItemStack.EMPTY;

        for(int i = 0; i < container.getContainerSize() && map.isEmpty(); ++i) {
            ItemStack current = container.getItem(i);
            if (current.is(ModSetup.ALLAY_MAP.get())) {
                map = current;
            }
        }

        map = map.copy();
        map.setCount(1);
        map.getOrCreateTag().putInt("map_scale_direction", 1);
        return map;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
}
