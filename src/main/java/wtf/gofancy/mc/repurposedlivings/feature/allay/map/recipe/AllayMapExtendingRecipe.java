package wtf.gofancy.mc.repurposedlivings.feature.allay.map.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import wtf.gofancy.mc.repurposedlivings.Capabilities;
import wtf.gofancy.mc.repurposedlivings.ModSetup;

import java.util.stream.IntStream;

public class AllayMapExtendingRecipe extends ShapedRecipe {

    public AllayMapExtendingRecipe(final ResourceLocation pId) {
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
    public boolean matches(final CraftingContainer container, final Level level) {
        if (super.matches(container, level)) {
            final ItemStack map = findMap(container);
            // this check is serverside only (would fail on client side anyway)
            return map.isEmpty() && !level.isClientSide &&
                level.getCapability(Capabilities.ALLAY_MAP_DATA).resolve()
                    .flatMap(data -> data.get(map))
                    .map(data -> data.getCorrespondingMapData(level))
                    .map(data -> data.scale < 4)
                    .orElse(false);
        }
        return false;
    }

    @Override
    public ItemStack assemble(final CraftingContainer container) {
        final ItemStack map = findMap(container).copy();
        map.setCount(1);
        map.getOrCreateTag().putInt("map_scale_direction", 1);
        return map;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }
    
    private ItemStack findMap(final Container container) {
        return IntStream.range(0, container.getContainerSize())
            .mapToObj(container::getItem)
            .filter(stack -> stack.is(ModSetup.ALLAY_MAP.get()))
            .findFirst()
            .orElse(ItemStack.EMPTY);
    }
}
