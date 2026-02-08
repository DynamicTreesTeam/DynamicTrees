package com.dtteam.dynamictrees.recipe;

import com.dtteam.dynamictrees.config.*;
import com.dtteam.dynamictrees.registry.*;
import com.dtteam.dynamictrees.systems.*;
import com.dtteam.dynamictrees.tree.species.*;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;

import java.util.*;

public class SeedConversionRecipe extends CustomRecipe {

    public SeedConversionRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if(craftingInput.items().stream().anyMatch(itemStack -> itemStack.is(DTRegistries.DIRT_BUCKET.get()))) {
            if (DTConfigs.COMMON.generateDirtBucketRecipes.get()) {
                for (Species species : Species.REGISTRY) {
                    for (SeedSaplingRecipe recipe : species.getPrimitiveSaplingRecipes()) {
                        if (!recipe.canCraftSaplingToSeed() && !recipe.canCraftSeedToSapling()) {
                            return false;
                        }
                        if (recipe.canCraftSaplingToSeed()) {
                            if (craftingInput.items().stream().filter(stack -> stack.is(recipe.getSaplingItem().get())).count() == 1)
                                if (new HashSet<>(craftingInput.items().stream().map(ItemStack::getItem).toList()).containsAll(recipe.getIngredientsForSaplingToSeed())) {
                                    return craftingInput.items().size() == recipe.getIngredientsForSaplingToSeed().size() + 2;
                                }
                        }

                        if (recipe.canCraftSeedToSapling()) {
                            if (craftingInput.items().stream().filter(stack -> stack.is(species.getSeed().get())).count() == 1) {
                                if (new HashSet<>(craftingInput.items().stream().map(ItemStack::getItem).toList()).containsAll(recipe.getIngredientsForSeedToSapling())) {
                                    return craftingInput.items().size() == recipe.getIngredientsForSeedToSapling().size() + 2;
                                }
                            }
                        }
                    }
                }
            }
        }
        if(DTConfigs.COMMON.generateMegaSeedRecipe.get()){
            for(Species species : Species.REGISTRY) {
                if(species.isMegaSpecies()) {
                    if (craftingInput.items().stream().filter(stack -> stack.is(species.getSeed().get())).count() == 4) {
                        return craftingInput.items().size() == 4;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider) {
        for(Species species : Species.REGISTRY) {
            for (SeedSaplingRecipe recipe : species.getPrimitiveSaplingRecipes()) {
                if (recipe.canCraftSaplingToSeed()) {
                    if (craftingInput.items().stream().filter(stack -> stack.is(recipe.getSaplingItem().get())).count() == 1)
                        if (new HashSet<>(craftingInput.items().stream().map(ItemStack::getItem).toList()).containsAll(recipe.getIngredientsForSaplingToSeed())) {
                            return species.getSeed().get().getDefaultInstance();
                        }
                }

                if (recipe.canCraftSeedToSapling()) {
                    if (craftingInput.items().stream().filter(stack -> stack.is(species.getSeed().get())).count() == 1) {
                        if (new HashSet<>(craftingInput.items().stream().map(ItemStack::getItem).toList()).containsAll(recipe.getIngredientsForSeedToSapling())) {
                            return  recipe.getSaplingItem().get().getDefaultInstance();
                        }
                    }
                }
            }
        }
        return ItemStack.EMPTY;
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
