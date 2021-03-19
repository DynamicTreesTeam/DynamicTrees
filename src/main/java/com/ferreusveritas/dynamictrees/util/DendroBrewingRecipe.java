package com.ferreusveritas.dynamictrees.util;

import com.ferreusveritas.dynamictrees.items.DendroPotion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.brewing.IBrewingRecipe;

/**
 * An implementation of {@link IBrewingRecipe} for the {@link DendroPotion} item.
 *
 * @author Harley O'Connor
 */
public final class DendroBrewingRecipe implements IBrewingRecipe {
	
	private final ItemStack input;
	private final ItemStack ingredient;
	private final ItemStack output;
	
	public DendroBrewingRecipe (final ItemStack input, final ItemStack ingredient, final ItemStack output) {
		this.input = input;
		this.ingredient = ingredient;
		this.output = output;
	}
	
	@Override
	public boolean isInput (final ItemStack inputStack) {
		// For transformation potion, only allow input if it doesn't already have a tree tag.
		return DendroPotion.getPotionType(inputStack) == DendroPotion.getPotionType(this.input) && !inputStack.getOrCreateTag().contains(DendroPotion.TREE_TAG_KEY);
	}
	
	@Override
	public boolean isIngredient (final ItemStack ingredientStack) {
		return this.ingredient.getItem().equals(ingredientStack.getItem());
	}
	
	@Override
	public ItemStack getOutput (final ItemStack inputStack, final ItemStack ingredientStack) {
		if (!inputStack.isEmpty() && !ingredientStack.isEmpty() && isIngredient(ingredientStack)) {
			// For transformation potion, only brew if it doesn't already have a tree tag (must check here too, in case potion is left in after being brewed).
			if (!inputStack.getOrCreateTag().contains(DendroPotion.TREE_TAG_KEY))
				return this.output.copy();
		}
		
		return ItemStack.EMPTY;
	}
	
	public ItemStack getInput() {
		return input;
	}
	
	public ItemStack getIngredient() {
		return ingredient;
	}
	
	public ItemStack getOutput() {
		return output;
	}
	
}
