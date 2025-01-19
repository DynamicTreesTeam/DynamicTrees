package com.dtteam.dynamictrees.data;

import com.dtteam.dynamictrees.DynamicTrees;
import com.dtteam.dynamictrees.registry.DTRegistries;
import com.dtteam.dynamictrees.tree.species.Species;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles programmatic recipes. These should be done sparingly and only for dynamic recipes - one-off recipes should be
 * defined in Json.
 *
 * @author Harley O'Connor
 */
public final class DirtBucketRecipeHandler {

    public static void registerDirtBucketRecipes(final Collection<RecipeHolder<?>> craftingRecipes) {
        for (final Species species : Species.REGISTRY.getAll()) {
            // If the species doesn't have a seed it doesn't need any recipes.
            if (!species.hasSeed()) {
                continue;
            }

            final ResourceLocation registryName = species.getRegistryName();

            species.getPrimitiveSaplingRecipes().forEach(saplingRecipe -> {
                final Item saplingItem = saplingRecipe.getSaplingItem().orElse(null);
                if (saplingItem == null) {
                    DynamicTrees.LOG.error("Error creating seed-sapling recipe for species \"{}\" as sapling item does not exist.", species.getRegistryName());
                    return;
                } else {
                    BuiltInRegistries.ITEM.getKey(saplingItem);
                }

                if (saplingRecipe.canCraftSaplingToSeed()) {
                    final ResourceLocation saplingToSeed = ResourceLocation.fromNamespaceAndPath(registryName.getNamespace(),
                            separate(BuiltInRegistries.ITEM.getKey(saplingItem)) + "_to_" + registryName.getPath() + "_seed");

                    List<Item> ingredients = saplingRecipe.getIngredientsForSaplingToSeed();
                    ingredients.add(DTRegistries.DIRT_BUCKET.get());
                    ingredients.add(saplingItem);
                    craftingRecipes.add(new RecipeHolder<>(saplingToSeed, createShapeless(saplingToSeed,
                            species.getSeedStack(1), //result
                            ingredients(ingredients)))); //ingredients
                }

                if (saplingRecipe.canCraftSeedToSapling()) {
                    final ResourceLocation seedToSapling = ResourceLocation.fromNamespaceAndPath(registryName.getNamespace(),
                            registryName.getPath() + "_seed_to_" + separate(BuiltInRegistries.ITEM.getKey(saplingItem)));

                    List<Item> ingredients = saplingRecipe.getIngredientsForSeedToSapling();
                    ingredients.add(DTRegistries.DIRT_BUCKET.get());
                    ingredients.add(species.getSeed().map(Item.class::cast).orElse(Items.AIR));
                    craftingRecipes.add(new RecipeHolder<>(seedToSapling, createShapeless(seedToSapling,
                            new ItemStack(saplingItem), //result
                            ingredients(ingredients)))); //ingredients
                }

            });
        }
    }

    private static String separate(final ResourceLocation resourceLocation) {
        return resourceLocation.getNamespace() + "_" + resourceLocation.getPath();
    }

    private static ShapelessRecipe createShapeless(final ResourceLocation registryName, final ItemStack out, final Ingredient... ingredients) {
        return new ShapelessRecipe("CRAFTING_MISC", CraftingBookCategory.MISC, out, NonNullList.of(Ingredient.EMPTY, ingredients));
    }

    private static Ingredient[] ingredients(Collection<Item> items) {
        return ingredients(items.toArray(new Item[]{}));
    }

    private static Ingredient[] ingredients(final Item... items) {
        if (items.length == 0) return new Ingredient[]{Ingredient.EMPTY};
        return Arrays.stream(items).map(item -> Ingredient.of(new ItemStack(item))).collect(Collectors.toSet()).toArray(new Ingredient[]{});
    }

}