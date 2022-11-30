package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.item.DendroPotion;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Harley O'Connor
 */
@JeiPlugin
public final class DTJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return DynamicTrees.location(DynamicTrees.MOD_ID);
    }

    @Override
    public void registerItemSubtypes(final ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(DTRegistries.DENDRO_POTION.get());
    }

    @Override
    public void registerRecipes(final IRecipeRegistration registration) {
        final IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        final List<IJeiBrewingRecipe> brewingRecipes = new ArrayList<>();

        DendroPotion.brewingRecipes.forEach(recipe ->
                brewingRecipes.add(makeJeiBrewingRecipe(factory, recipe.getInput(), recipe.getIngredient(), recipe.getOutput())));

        registration.addRecipes(RecipeTypes.BREWING, brewingRecipes);
    }

    private static IJeiBrewingRecipe makeJeiBrewingRecipe(IVanillaRecipeFactory factory, final ItemStack inputStack, final ItemStack ingredientStack, ItemStack output) {
        return factory.createBrewingRecipe(Collections.singletonList(ingredientStack), inputStack, output);
    }

}
