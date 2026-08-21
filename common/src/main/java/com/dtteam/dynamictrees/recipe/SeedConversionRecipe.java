package com.dtteam.dynamictrees.recipe;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.systems.SeedSaplingRecipe;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

public class SeedConversionRecipe extends CustomRecipe {

    public SeedConversionRecipe() {
        super();
    }

    public boolean matches(CraftingInput craftingInput, Level level) {
        if(DTConfigs.COMMON.generateDirtBucketRecipes.get() && hasDirtBucket(craftingInput)) {
            for (Species species : Species.REGISTRY) {
                for (SeedSaplingRecipe recipe : species.getPrimitiveSaplingRecipes()) {
                    if (!recipe.canCraftSaplingToSeed() && !recipe.canCraftSeedToSapling()) {
                        return false;
                    }
                    if (saplingToSeedCondition(craftingInput, recipe)) {
                        return hasExactCount(craftingInput, recipe.getIngredientsForSaplingToSeed());
                    }
                    if (seedToSaplingCondition(craftingInput, species, recipe)) {
                        return hasExactCount(craftingInput, recipe.getIngredientsForSeedToSapling());
                    }
                }
            }
        }
        return false;
    }

    public ItemStack assemble(CraftingInput craftingInput) {
        for(Species species : Species.REGISTRY) {
            for (SeedSaplingRecipe recipe : species.getPrimitiveSaplingRecipes()) {
                if (saplingToSeedCondition(craftingInput, recipe)) {
                    return species.getSeed().get().getDefaultInstance();
                }
                if (seedToSaplingCondition(craftingInput, species, recipe)) {
                    return  recipe.getSaplingItem().get().getDefaultInstance();
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean seedToSaplingCondition(CraftingInput craftingInput, Species species, SeedSaplingRecipe recipe) {
        return recipe.canCraftSeedToSapling()
                && containsOneOfItem(craftingInput, species.getSeed().get())
                && containsAllIngredients(craftingInput, recipe.getIngredientsForSeedToSapling());
    }

    private static boolean saplingToSeedCondition(CraftingInput craftingInput, SeedSaplingRecipe recipe) {
        return recipe.canCraftSaplingToSeed()
                && containsOneOfItem(craftingInput, recipe.getSaplingItem().get())
                && containsAllIngredients(craftingInput, recipe.getIngredientsForSaplingToSeed());
    }

    private static boolean hasExactCount(CraftingInput craftingInput, List<Item> ingredients) {
        return notEmptyInput(craftingInput).count() == ingredients.size() + 2;
    }

    private static boolean containsAllIngredients(CraftingInput craftingInput, List<Item> ingredients) {
        return new HashSet<>(notEmptyInput(craftingInput).map(ItemStack::getItem).toList()).containsAll(ingredients);
    }

    private static boolean containsOneOfItem(CraftingInput craftingInput, Item item) {
        return craftingInput.items().stream().filter(stack -> stack.is(item)).count() == 1;
    }

    private static boolean hasDirtBucket(CraftingInput craftingInput) {
        return craftingInput.items().stream().anyMatch(itemStack -> itemStack.is(DTRegistries.DIRT_BUCKET.get()));
    }

    private static Stream<ItemStack> notEmptyInput(CraftingInput craftingInput) {
        return craftingInput.items().stream().filter(s -> !s.isEmpty());
    }

    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return DTRegistries.SEED_CONVERSION_RECIPE_TYPE.get();
    }

}
