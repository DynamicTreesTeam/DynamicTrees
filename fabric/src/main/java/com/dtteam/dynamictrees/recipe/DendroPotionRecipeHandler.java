package com.dtteam.dynamictrees.recipe;

import com.dtteam.dynamictrees.config.DTConfigs;
import com.dtteam.dynamictrees.item.DendroPotion;
import com.dtteam.dynamictrees.registry.DTRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class DendroPotionRecipeHandler {

    private static final List<DendroBrewingMix> brewingRecipes = new ArrayList<>();
    private static final List<ItemStack> ingredients = new ArrayList<>();

    public static boolean isIngredient(ItemStack stack){
        AtomicBoolean found = new AtomicBoolean(false);
        ingredients.forEach(ingredient -> {
            if (ItemStack.isSameItemSameComponents(ingredient, stack))
                found.set(true);
        });
        return found.get();
    }

    public static List<DendroBrewingMix> getAllDendroRecipes() {
        if (!brewingRecipes.isEmpty()) return brewingRecipes;

        ItemStack biocharIngredient = new ItemStack(Items.CHARCOAL);
        final ItemStack baseStack = setPotion(new ItemStack(Items.POTION), DTConfigs.COMMON.biocharBrewingBase.get());
        brewingRecipes.add(getRecipe(baseStack, biocharIngredient, getPotionStack(DendroPotion.DendroPotionType.BIOCHAR)));
        ingredients.add(biocharIngredient);

        //Regular potions
        for (int i = 1; i < DendroPotion.DendroPotionType.values().length; i++) {
            final DendroPotion.DendroPotionType type = DendroPotion.DendroPotionType.values()[i];

            if (!type.isActive()) continue;

            brewingRecipes.add(getRecipe(type.getIngredient(), type));
            ingredients.add(type.getIngredient());
        }

        return brewingRecipes;
    }

    public static ItemStack setPotion(ItemStack pStack, String potionName) {
        Optional<Holder.Reference<Potion>> potion = BuiltInRegistries.POTION.getHolder(ResourceKey.create(Registries.POTION, Identifier.parse(potionName)));
        potion.ifPresent(holder -> pStack.set(DataComponents.POTION_CONTENTS, new PotionContents(holder)));

        return pStack;
    }

    private static DendroBrewingMix getRecipe(ItemStack ingredient, DendroPotion.DendroPotionType typeOut) {
        return getRecipe(getPotionStack(typeOut.getBasePotionType()), ingredient, getPotionStack(typeOut));
    }

    private static DendroBrewingMix getRecipe(ItemStack stackIn, ItemStack ingredientStack, ItemStack stackOut) {
        return new DendroBrewingMix(stackIn, ingredientStack, stackOut);
    }

    private static ItemStack getPotionStack(DendroPotion.DendroPotionType type) {
        return DendroPotion.applyIndexTag(new ItemStack(DTRegistries.DENDRO_POTION.get()), type.getIndex());
    }

}
