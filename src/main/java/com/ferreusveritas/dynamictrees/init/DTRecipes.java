package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.RecipeRegistryEvent;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import net.minecraft.client.util.RecipeBookCategories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.stream.Stream;

/**
 * Handles programmatic recipes. These should be done sparingly and only for dynamic
 * recipes - one-off recipes should be defined in Json.
 *
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber
public final class DTRecipes {

    @SubscribeEvent
    public static void registerDirtBucketRecipes(final RecipeRegistryEvent event) {
        if (event.getType() != IRecipeType.CRAFTING || !DTConfigs.GENERATE_DIRT_BUCKET_RECIPES.get())
            return;

        for (final Species species : Species.REGISTRY.getAll()) {
            // If the species doesn't have a seed it doesn't need any recipes.
            if (!species.hasSeed())
                continue;

            final ResourceLocation saplingToSeed = ResourceLocationUtils.suffix(species.getRegistryName(), "_to_seed");
            final ResourceLocation seedToSapling = ResourceLocationUtils.suffix(species.getRegistryName(), "_to_sapling");

            species.getPrimitiveSaplingItems().forEach(primitiveSapling -> {
                event.registerIfAbsent(saplingToSeed, createShapeless(saplingToSeed, species.getSeedStack(1),
                        ingredient(DTRegistries.DIRT_BUCKET), ingredient(primitiveSapling)));
                event.registerIfAbsent(seedToSapling, createShapeless(seedToSapling, new ItemStack(primitiveSapling),
                        ingredient(DTRegistries.DIRT_BUCKET), ingredient(species.getSeed().map(Item.class::cast).orElse(Items.AIR))));
            });
        }
    }

    private static ShapelessRecipe createShapeless(final ResourceLocation registryName, final ItemStack out, final Ingredient... ingredients) {
        return new ShapelessRecipe(registryName, RecipeBookCategories.CRAFTING_MISC.name(),
                out, NonNullList.of(Ingredient.EMPTY, ingredients));
    }

    private static Ingredient ingredient (final Item item) {
        return Ingredient.fromValues(Stream.of(new Ingredient.SingleItemList(new ItemStack(item))));
    }

}
