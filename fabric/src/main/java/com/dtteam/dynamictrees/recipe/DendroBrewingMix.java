package com.dtteam.dynamictrees.recipe;

import net.minecraft.world.item.ItemStack;

/**

 * @author MaxHyper
 */
public record DendroBrewingMix(ItemStack input, ItemStack ingredient, ItemStack output) {

	public boolean isInput(final ItemStack inputStack) {
		return ItemStack.isSameItemSameComponents(input, inputStack);
	}

	public boolean isIngredient(final ItemStack ingredientStack) {
		return ItemStack.isSameItemSameComponents(ingredient, ingredientStack);
	}

	public ItemStack getOutput(final ItemStack inputStack, final ItemStack ingredientStack) {
		// We need to apply logic for the brewing or simply the ingredient defines the output and any input was allowed
		// A smarter way would be nice, but it works
		if (!inputStack.isEmpty() && !ingredientStack.isEmpty() && isIngredient(ingredientStack) && isInput(inputStack)) {
			return this.output.copy();
		}
		return ItemStack.EMPTY;
	}


}
