package com.ferreusveritas.dynamictrees.recipe;

import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.systems.SeedSaplingRecipe;
import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.core.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.stream.Stream;

public class SeedConversionRecipe extends CustomRecipe {

    public SeedConversionRecipe(ResourceLocation pId, CraftingBookCategory pCategory) {
        super(pId, pCategory);
    }
    @Override
    public boolean matches(CraftingContainer craftingInput, Level level) {
        if(DTConfigs.GENERATE_DIRT_BUCKET_RECIPES.get() && hasDirtBucket(craftingInput)) {
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

    @Override
    public ItemStack assemble(CraftingContainer craftingInput, RegistryAccess registryAccess) {
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

    private static boolean seedToSaplingCondition(CraftingContainer craftingInput, Species species, SeedSaplingRecipe recipe) {
        return recipe.canCraftSeedToSapling()
                && containsOneOfItem(craftingInput, species.getSeed().get())
                && containsAllIngredients(craftingInput, recipe.getIngredientsForSeedToSapling());
    }

    private static boolean saplingToSeedCondition(CraftingContainer craftingInput, SeedSaplingRecipe recipe) {
        return recipe.canCraftSaplingToSeed()
                && containsOneOfItem(craftingInput, recipe.getSaplingItem().get())
                && containsAllIngredients(craftingInput, recipe.getIngredientsForSaplingToSeed());
    }

    private static boolean hasExactCount(CraftingContainer craftingInput, List<Item> ingredients) {
        return notEmptyInput(craftingInput).count() == ingredients.size() + 2;
    }

    private static boolean containsAllIngredients(CraftingContainer craftingInput, List<Item> ingredients) {
        return new HashSet<>(notEmptyInput(craftingInput).map(ItemStack::getItem).toList()).containsAll(ingredients);
    }

    private static boolean containsOneOfItem(CraftingContainer craftingInput, Item item) {
        return craftingInput.getItems().stream().filter(stack -> stack.is(item)).count() == 1;
    }

    private static boolean hasDirtBucket(CraftingContainer craftingInput) {
        return craftingInput.getItems().stream().anyMatch(itemStack -> itemStack.is(DTRegistries.DIRT_BUCKET.get()));
    }

    private static @NonNull Stream<ItemStack> notEmptyInput(CraftingContainer craftingInput) {
        return craftingInput.getItems().stream().filter(s -> !s.isEmpty());
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return DTRegistries.SEED_CONVERSION_RECIPE_TYPE.get();
    }



}
