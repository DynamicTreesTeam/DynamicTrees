package com.dtteam.dynamictrees.util;

import com.dtteam.dynamictrees.item.DendroPotion;
import net.minecraft.world.item.ItemStack;

/**
 * An implementation of {@link IBrewingRecipe} for the {@link DendroPotion} item.
 *
 * @author Harley O'Connor
 */
//public record DendroBrewingRecipe(ItemStack input, ItemStack ingredient, ItemStack output) implements IBrewingRecipe {
//
//	@Override
//	public boolean isInput(final ItemStack inputStack) {
//		return ItemStack.isSameItemSameTags(input, inputStack);
//	}
//
//	@Override
//	public boolean isIngredient(final ItemStack ingredientStack) {
//		return ItemStack.isSameItemSameTags(ingredient, ingredientStack);
//	}
//
//	@Override
//	public ItemStack getOutput(final ItemStack inputStack, final ItemStack ingredientStack) {
//		// We need to apply logic for the brewing or simply the ingredient defines the output and any input was allowed
//		// A smarter way would be nice, but it works
//		if (!inputStack.isEmpty() && !ingredientStack.isEmpty() && isIngredient(ingredientStack) && isInput(inputStack)) {
//			return this.output.copy();
//		}
//		return ItemStack.EMPTY;
//	}
//
//
//}
