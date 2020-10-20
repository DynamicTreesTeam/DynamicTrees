package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.items.DendroPotion;
import com.google.common.collect.Lists;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Harley O'Connor
 */
@JeiPlugin
public final class DTJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(DynamicTrees.MODID, DynamicTrees.MODID);
    }

    @Override
    public void registerItemSubtypes (final ISubtypeRegistration registration) {
        registration.useNbtForSubtypes(DTRegistries.dendroPotion);
    }

    @Override
    public void registerRecipes (final IRecipeRegistration registration) {
        final IVanillaRecipeFactory factory = registration.getVanillaRecipeFactory();
        final List<IJeiBrewingRecipe> brewingRecipes = new ArrayList<>();

        DendroPotion.brewingRecipes.forEach(recipe -> brewingRecipes.add(makeJeiBrewingRecipe(factory, recipe.getInput(), recipe.getIngredient(), recipe.getOutput())));

        registration.addRecipes(brewingRecipes, VanillaRecipeCategoryUid.BREWING);
    }

    public static IJeiBrewingRecipe makeJeiBrewingRecipe(IVanillaRecipeFactory factory, final ItemStack inputStack, final ItemStack ingredientStack, ItemStack output) {
        return factory.createBrewingRecipe(Arrays.asList(ingredientStack), inputStack, output);
    }

}
