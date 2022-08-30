package com.ferreusveritas.dynamictrees.data;

import com.ferreusveritas.dynamictrees.init.DTRegistries;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles programmatic recipes. These should be done sparingly and only for dynamic recipes - one-off recipes should be
 * defined in Json.
 *
 * @author Harley O'Connor
 */
public final class DTRecipes {

    public static void registerDirtBucketRecipes(final Map<ResourceLocation, Recipe<?>> craftingRecipes) {
        for (final Species species : Species.REGISTRY.getAll()) {
            // If the species doesn't have a seed it doesn't need any recipes.
            if (!species.hasSeed()) {
                continue;
            }

            final ResourceLocation registryName = species.getRegistryName();

            species.getPrimitiveSaplingRecipes().forEach(saplingRecipe -> {
                final Item saplingItem = saplingRecipe.getSaplingItem().orElse(null);
                if (saplingItem == null || ForgeRegistries.ITEMS.getKey(saplingItem) == null) {
                    LogManager.getLogger().error("Error creating seed-sapling recipe for species \"" + species.getRegistryName() + "\" as sapling item does not exist.");
                    return;
                }

                if (saplingRecipe.canCraftSaplingToSeed()) {
                    final ResourceLocation saplingToSeed = new ResourceLocation(registryName.getNamespace(),
                            separate(ForgeRegistries.ITEMS.getKey(saplingItem)) + "_to_" + registryName.getPath() + "_seed");

                    List<Item> ingredients = saplingRecipe.getIngredientsForSaplingToSeed();
                    ingredients.add(DTRegistries.DIRT_BUCKET.get());
                    ingredients.add(saplingItem);
                    craftingRecipes.putIfAbsent(saplingToSeed, createShapeless(saplingToSeed,
                            species.getSeedStack(1), //result
                            ingredients(ingredients))); //ingredients
                }

                if (saplingRecipe.canCraftSeedToSapling()) {
                    final ResourceLocation seedToSapling = new ResourceLocation(registryName.getNamespace(),
                            registryName.getPath() + "_seed_to_" + separate(ForgeRegistries.ITEMS.getKey(saplingItem)));

                    List<Item> ingredients = saplingRecipe.getIngredientsForSeedToSapling();
                    ingredients.add(DTRegistries.DIRT_BUCKET.get());
                    ingredients.add(species.getSeed().map(Item.class::cast).orElse(Items.AIR));
                    craftingRecipes.putIfAbsent(seedToSapling, createShapeless(seedToSapling,
                            new ItemStack(saplingItem), //result
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
        return Arrays.stream(items).map(item -> Ingredient.of(new ItemStack(item))).collect(Collectors.toSet()).toArray(new Ingredient[]{});
    }

}
