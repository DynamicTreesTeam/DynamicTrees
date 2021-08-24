package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles programmatic recipes. These should be done sparingly and only for dynamic recipes - one-off recipes should be
 * defined in Json.
 *
 * @author Harley O'Connor
 */
public final class DTRecipes {

    public static void registerDirtBucketRecipes(final Map<ResourceLocation, IRecipe<?>> craftingRecipes) {
        for (final Species species : Species.REGISTRY.getAll()) {
            // If the species doesn't have a seed it doesn't need any recipes.
            if (!species.hasSeed()) {
                continue;
            }

            final ResourceLocation registryName = species.getRegistryName();

            species.getPrimitiveSaplingRecipes().forEach(saplingRecipe -> {
                assert saplingRecipe.isValid() && saplingRecipe.getSaplingItem().getRegistryName() != null;

                if (saplingRecipe.canCraftSaplingToSeed()) {
                    final ResourceLocation saplingToSeed = new ResourceLocation(registryName.getNamespace(),
                            separate(saplingRecipe.getSaplingItem().getRegistryName()) + "_to_" + registryName.getPath() + "_seed");

                    List<Item> ingredients = saplingRecipe.getIngredientsForSaplingToSeed();
                    ingredients.add(DTRegistries.DIRT_BUCKET);
                    ingredients.add(saplingRecipe.getSaplingItem());
                    craftingRecipes.putIfAbsent(saplingToSeed, createShapeless(saplingToSeed,
                            species.getSeedStack(1), //result
                            ingredients(ingredients))); //ingredients
                }

                if (saplingRecipe.canCraftSeedToSapling()) {
                    final ResourceLocation seedToSapling = new ResourceLocation(registryName.getNamespace(),
                            registryName.getPath() + "_seed_to_" + separate(saplingRecipe.getSaplingItem().getRegistryName()));

                    List<Item> ingredients = saplingRecipe.getIngredientsForSeedToSapling();
                    ingredients.add(DTRegistries.DIRT_BUCKET);
                    ingredients.add(species.getSeed().map(Item.class::cast).orElse(Items.AIR));
                    craftingRecipes.putIfAbsent(seedToSapling, createShapeless(seedToSapling,
                            new ItemStack(saplingRecipe.getSaplingItem()), //result
                            ingredients(ingredients))); //ingredients
                }

            });
        }
    }

    private static String separate(final ResourceLocation resourceLocation) {
        return resourceLocation.getNamespace() + "_" + resourceLocation.getPath();
    }

    private static ShapelessRecipe createShapeless(final ResourceLocation registryName, final ItemStack out, final Ingredient... ingredients) {
        return new ShapelessRecipe(registryName, "CRAFTING_MISC", out, NonNullList.of(Ingredient.EMPTY, ingredients));
    }

    private static Ingredient[] ingredients(Collection<Item> items) {
        return ingredients(items.toArray(new Item[]{}));
    }
    private static Ingredient[] ingredients(final Item... items) {
        if (items.length == 0) return new Ingredient[]{Ingredient.EMPTY};
        return Arrays.stream(items).map(item->Ingredient.of(new ItemStack(item))).collect(Collectors.toSet()).toArray(new Ingredient[]{});
    }

}
