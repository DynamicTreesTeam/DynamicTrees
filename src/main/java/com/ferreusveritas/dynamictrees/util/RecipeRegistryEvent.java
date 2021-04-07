package com.ferreusveritas.dynamictrees.util;

import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class RecipeRegistryEvent extends Event {

    private final IRecipeType<?> type;
    private final Map<ResourceLocation, IRecipe<?>> recipes;

    public RecipeRegistryEvent(final IRecipeType<?> recipeType, Map<ResourceLocation, IRecipe<?>> recipes) {
        this.type = recipeType;
        this.recipes = recipes;
    }

    public RecipeRegistryEvent register(final ResourceLocation resourceLocation, final IRecipe<?> recipe) {
        this.recipes.put(resourceLocation, recipe);
        return this;
    }

    public RecipeRegistryEvent registerIfAbsent(final ResourceLocation resourceLocation, final IRecipe<?> recipe) {
        this.recipes.putIfAbsent(resourceLocation, recipe);
        return this;
    }

    public IRecipeType<?> getType() {
        return type;
    }

    public Map<ResourceLocation, IRecipe<?>> getRecipes() {
        return recipes;
    }

}
