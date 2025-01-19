package com.dtteam.dynamictrees.recipe;

import com.dtteam.dynamictrees.item.DendroPotion;
import com.dtteam.dynamictrees.platform.Services;
import com.dtteam.dynamictrees.platform.services.IConfigHelper;
import com.dtteam.dynamictrees.registry.DTRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DendroPotionRecipeHandler {

    public static List<DendroBrewingRecipe> getAllDendroRecipes() {
        List<DendroBrewingRecipe> brewingRecipes = new ArrayList<>();

        //Biochar potion
        final ItemStack baseStack = setPotion(new ItemStack(Items.POTION), Services.CONFIG.getStringConfig(IConfigHelper.BIOCHAR_BREWING_BASE));
        brewingRecipes.add(getRecipe(baseStack, new ItemStack(Items.CHARCOAL), getPotionStack(DendroPotion.DendroPotionType.BIOCHAR)));

        //Regular potions
        for (int i = 1; i < DendroPotion.DendroPotionType.values().length; i++) {
            final DendroPotion.DendroPotionType type = DendroPotion.DendroPotionType.values()[i];

            if (!type.isActive()) continue;

            brewingRecipes.add(getRecipe(type.getIngredient(), type));
        }

        return brewingRecipes;
    }

    public static ItemStack setPotion(ItemStack pStack, String potionName) {
        Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION.getHolder(ResourceKey.create(Registries.POTION, ResourceLocation.parse(potionName)));
        potion.ifPresent(holder -> pStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder)));

        return pStack;
    }

    private static DendroBrewingRecipe getRecipe(ItemStack ingredient, DendroPotion.DendroPotionType typeOut) {
        return getRecipe(getPotionStack(typeOut.getBasePotionType()), ingredient, getPotionStack(typeOut));
    }

    private static DendroBrewingRecipe getRecipe(ItemStack stackIn, ItemStack ingredientStack, ItemStack stackOut) {
        return new DendroBrewingRecipe(stackIn, ingredientStack, stackOut);
    }

    private static ItemStack getPotionStack(DendroPotion.DendroPotionType type) {
        return DendroPotion.applyIndexTag(new ItemStack(DTRegistries.DENDRO_POTION.get()), type.getIndex());
    }

}
