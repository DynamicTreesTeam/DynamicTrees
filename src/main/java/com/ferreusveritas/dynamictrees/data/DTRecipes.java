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

import java.util.Map;
import java.util.stream.Stream;

/**
 * Handles programmatic recipes. These should be done sparingly and only for dynamic
 * recipes - one-off recipes should be defined in Json.
 *
 * @author Harley O'Connor
 */
public final class DTRecipes {

    public static void registerDirtBucketRecipes(final Map<ResourceLocation, IRecipe<?>> craftingRecipes) {
        for (final Species species : Species.REGISTRY.getAll()) {
            // If the species doesn't have a seed it doesn't need any recipes.
            if (!species.hasSeed())
                continue;

            final ResourceLocation registryName = species.getRegistryName();

            species.getPrimitiveSaplingItems().forEach(primitiveSapling -> {
                assert primitiveSapling.getRegistryName() != null;

                if (species.canCraftSaplingToSeed()){
                    final ResourceLocation saplingToSeed = new ResourceLocation(registryName.getNamespace(),
                            separate(primitiveSapling.getRegistryName()) + "_to_" + registryName.getPath() + "_seed");
                    craftingRecipes.putIfAbsent(saplingToSeed, createShapeless(saplingToSeed, species.getSeedStack(1),
                            ingredient(DTRegistries.DIRT_BUCKET), ingredient(primitiveSapling)));

                }

                if (species.canCraftSeedToSapling()){
                    final ResourceLocation seedToSapling = new ResourceLocation(registryName.getNamespace(),
                            registryName.getPath() + "_seed_to_" + separate(primitiveSapling.getRegistryName()));
                    craftingRecipes.putIfAbsent(seedToSapling, createShapeless(seedToSapling, new ItemStack(primitiveSapling),
                            ingredient(DTRegistries.DIRT_BUCKET), ingredient(species.getSeed().map(Item.class::cast).orElse(Items.AIR))));
                }

            });
        }
    }

    private static String separate (final ResourceLocation resourceLocation) {
        return resourceLocation.getNamespace() + "_" + resourceLocation.getPath();
    }

    private static ShapelessRecipe createShapeless(final ResourceLocation registryName, final ItemStack out, final Ingredient... ingredients) {
        return new ShapelessRecipe(registryName, "CRAFTING_MISC", out, NonNullList.of(Ingredient.EMPTY, ingredients));
    }

    private static Ingredient ingredient (final Item item) {
        return Ingredient.fromValues(Stream.of(new Ingredient.SingleItemList(new ItemStack(item))));
    }

}
